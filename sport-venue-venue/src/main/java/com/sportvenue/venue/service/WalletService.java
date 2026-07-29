package com.sportvenue.venue.service;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.venue.entity.CustomerWallet;
import com.sportvenue.venue.entity.WalletLedger;
import com.sportvenue.venue.entity.WalletRechargeOrder;
import com.sportvenue.venue.repository.CustomerWalletRepository;
import com.sportvenue.venue.repository.WalletLedgerRepository;
import com.sportvenue.venue.repository.WalletRechargeOrderRepository;
import com.sportvenue.venue.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WalletService {

    @Autowired
    private CustomerWalletRepository walletRepository;
    @Autowired
    private WalletLedgerRepository ledgerRepository;
    @Autowired
    private WalletRechargeOrderRepository rechargeOrderRepository;
    @Autowired
    private MerchantFeatureService featureService;

    public CustomerWallet getOrCreate(Long merchantId, Long customerId) {
        return walletRepository.findByMerchantIdAndCustomerUserId(merchantId, customerId)
                .orElseGet(() -> {
                    CustomerWallet w = new CustomerWallet();
                    w.setMerchantId(merchantId);
                    w.setCustomerUserId(customerId);
                    w.setBalance(BigDecimal.ZERO);
                    w.setUpdateTime(LocalDateTime.now());
                    return walletRepository.save(w);
                });
    }

    public Map<String, Object> myWallet() {
        Long merchantId = SecurityUtils.requireCustomerMerchantId();
        Long customerId = SecurityUtils.requireCustomerId();
        CustomerWallet w = getOrCreate(merchantId, customerId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("balance", w.getBalance());
        m.put("customerId", customerId);
        m.put("merchantId", merchantId);
        return m;
    }

    public List<WalletLedger> myLedgers() {
        Long merchantId = SecurityUtils.requireCustomerMerchantId();
        Long customerId = SecurityUtils.requireCustomerId();
        return ledgerRepository.findTop50ByMerchantIdAndCustomerUserIdOrderByCreateTimeDesc(merchantId, customerId);
    }

    @Transactional
    public Map<String, Object> createRecharge(BigDecimal amount) {
        Long merchantId = SecurityUtils.requireCustomerMerchantId();
        Long customerId = SecurityUtils.requireCustomerId();
        featureService.requireEnabled(merchantId, MerchantFeatureService.Feature.RECHARGE);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "充值金额须大于0");
        }
        WalletRechargeOrder order = new WalletRechargeOrder();
        order.setOrderNo("WR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000)));
        order.setMerchantId(merchantId);
        order.setCustomerUserId(customerId);
        order.setAmount(amount);
        order.setStatus(WalletRechargeOrder.Status.PENDING);
        order.setPayChannel("MOCK");
        order = rechargeOrderRepository.save(order);

        // 本地 mock：创建即支付成功入账
        confirmPaid(order.getOrderNo());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderNo", order.getOrderNo());
        m.put("amount", amount);
        m.put("status", "PAID");
        m.put("payMode", "MOCK");
        return m;
    }

    @Transactional
    public void confirmPaid(String orderNo) {
        WalletRechargeOrder order = rechargeOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "充值单不存在"));
        if (order.getStatus() == WalletRechargeOrder.Status.PAID) {
            return; // 幂等
        }
        if (order.getStatus() != WalletRechargeOrder.Status.PENDING) {
            throw new BusinessException(400, "充值单状态不可支付");
        }
        credit(order.getMerchantId(), order.getCustomerUserId(), order.getAmount(),
                "RECHARGE", order.getId(), "充值入账");
        order.setStatus(WalletRechargeOrder.Status.PAID);
        order.setPaidAt(LocalDateTime.now());
        rechargeOrderRepository.save(order);
    }

    @Transactional
    public void debit(Long merchantId, Long customerId, BigDecimal amount, String bizType, Long bizId, String remark) {
        CustomerWallet w = getOrCreate(merchantId, customerId);
        if (w.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(40201, "余额不足");
        }
        w.setBalance(w.getBalance().subtract(amount));
        w.setUpdateTime(LocalDateTime.now());
        walletRepository.save(w);
        saveLedger(merchantId, customerId, amount.negate(), w.getBalance(), bizType, bizId, remark);
    }

    @Transactional
    public void credit(Long merchantId, Long customerId, BigDecimal amount, String bizType, Long bizId, String remark) {
        CustomerWallet w = getOrCreate(merchantId, customerId);
        w.setBalance(w.getBalance().add(amount));
        w.setUpdateTime(LocalDateTime.now());
        walletRepository.save(w);
        saveLedger(merchantId, customerId, amount, w.getBalance(), bizType, bizId, remark);
    }

    private void saveLedger(Long merchantId, Long customerId, BigDecimal change, BigDecimal after,
                            String bizType, Long bizId, String remark) {
        WalletLedger ledger = new WalletLedger();
        ledger.setMerchantId(merchantId);
        ledger.setCustomerUserId(customerId);
        ledger.setChangeAmount(change);
        ledger.setBalanceAfter(after);
        ledger.setBizType(bizType);
        ledger.setBizId(bizId);
        ledger.setRemark(remark);
        ledgerRepository.save(ledger);
    }
}
