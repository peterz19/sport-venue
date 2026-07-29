package com.sportvenue.venue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.venue.entity.*;
import com.sportvenue.venue.repository.MerchantCommissionRuleRepository;
import com.sportvenue.venue.repository.MerchantRepository;
import com.sportvenue.venue.repository.PlatformCommissionEntryRepository;
import com.sportvenue.venue.repository.PlatformCommissionSettlementRepository;
import com.sportvenue.venue.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlatformCommissionService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MerchantCommissionRuleRepository ruleRepository;
    @Autowired
    private PlatformCommissionEntryRepository entryRepository;
    @Autowired
    private PlatformCommissionSettlementRepository settlementRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public MerchantCommissionRule ensureDefaultRule(Long merchantId) {
        return ruleRepository.findById(merchantId)
                .orElseGet(() -> ruleRepository.save(MerchantCommissionRule.defaults(merchantId)));
    }

    public MerchantCommissionRule getRule(Long merchantId) {
        return ensureDefaultRule(merchantId);
    }

    @Transactional
    public MerchantCommissionRule updateRule(Long merchantId, MerchantCommissionRule patch) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new BusinessException(404, "商户不存在");
        }
        MerchantCommissionRule rule = ensureDefaultRule(merchantId);
        if (patch.getRate() != null) {
            if (patch.getRate().compareTo(BigDecimal.ZERO) < 0 || patch.getRate().compareTo(BigDecimal.ONE) > 0) {
                throw new BusinessException(400, "抽成比例须在 0～1 之间（如 0.03 表示 3%）");
            }
            rule.setRate(patch.getRate());
        }
        if (patch.getIncludeCash() != null) rule.setIncludeCash(patch.getIncludeCash());
        if (patch.getIncludeWechat() != null) rule.setIncludeWechat(patch.getIncludeWechat());
        if (patch.getIncludeAlipay() != null) rule.setIncludeAlipay(patch.getIncludeAlipay());
        if (patch.getEnabled() != null) rule.setEnabled(patch.getEnabled());
        if (patch.getRemark() != null) rule.setRemark(patch.getRemark());
        rule.setUpdateTime(LocalDateTime.now());
        try {
            rule.setUpdateBy(SecurityUtils.requireCurrentUser().getId());
        } catch (Exception ignored) {
        }
        return ruleRepository.save(rule);
    }

    /**
     * 销售单支付成功后入账（幂等：同一 biz 只记一次）
     */
    @Transactional
    public void accrueFromSalesOrder(SalesOrder order) {
        if (order == null || order.getStatus() != SalesOrder.OrderStatus.PAID) {
            return;
        }
        if (entryRepository.findByBizTypeAndBizId(PlatformCommissionEntry.BIZ_SALES_ORDER, order.getId()).isPresent()) {
            return;
        }
        MerchantCommissionRule rule = ensureDefaultRule(order.getMerchantId());
        if (!Boolean.TRUE.equals(rule.getEnabled())) {
            return;
        }
        if (rule.getRate() == null || rule.getRate().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        SalesOrder.PayMethod method = order.getPayMethod();
        if (method == null || !matchPayMethod(rule, method)) {
            return;
        }

        BigDecimal amount = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        BigDecimal commission = amount.multiply(rule.getRate()).setScale(2, RoundingMode.HALF_UP);
        if (commission.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        PlatformCommissionEntry entry = new PlatformCommissionEntry();
        entry.setMerchantId(order.getMerchantId());
        entry.setBizType(PlatformCommissionEntry.BIZ_SALES_ORDER);
        entry.setBizId(order.getId());
        entry.setOrderNo(order.getOrderNo());
        entry.setPayMethod(method.name());
        entry.setOrderAmount(amount);
        entry.setRate(rule.getRate());
        entry.setCommissionAmount(commission);
        entry.setStatus(PlatformCommissionEntry.EntryStatus.PENDING);
        entry.setPaidAt(order.getPaidAt() != null ? order.getPaidAt() : LocalDateTime.now());
        entryRepository.save(entry);
        log.info("平台抽成入账 merchantId={} orderId={} commission={}", order.getMerchantId(), order.getId(), commission);
    }

    private boolean matchPayMethod(MerchantCommissionRule rule, SalesOrder.PayMethod method) {
        return switch (method) {
            case CASH -> Boolean.TRUE.equals(rule.getIncludeCash());
            case WECHAT -> Boolean.TRUE.equals(rule.getIncludeWechat());
            case ALIPAY -> Boolean.TRUE.equals(rule.getIncludeAlipay());
        };
    }

    public List<Map<String, Object>> merchantSummaries() {
        Map<Long, BigDecimal> pendingMap = new HashMap<>();
        Map<Long, Long> countMap = new HashMap<>();
        for (Object[] row : entryRepository.aggregatePendingByMerchant()) {
            Long mid = (Long) row[0];
            pendingMap.put(mid, (BigDecimal) row[1]);
            countMap.put(mid, (Long) row[2]);
        }
        return merchantRepository.findAll().stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("merchantId", m.getId());
            map.put("merchantCode", m.getMerchantCode());
            map.put("merchantName", m.getName());
            map.put("status", m.getStatus().name());
            MerchantCommissionRule rule = ensureDefaultRule(m.getId());
            map.put("rate", rule.getRate());
            map.put("ruleEnabled", rule.getEnabled());
            map.put("includeCash", rule.getIncludeCash());
            map.put("includeWechat", rule.getIncludeWechat());
            map.put("includeAlipay", rule.getIncludeAlipay());
            map.put("pendingCommission", pendingMap.getOrDefault(m.getId(), BigDecimal.ZERO));
            map.put("pendingEntryCount", countMap.getOrDefault(m.getId(), 0L));
            return map;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> merchantDetail(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException(404, "商户不存在"));
        MerchantCommissionRule rule = ensureDefaultRule(merchantId);
        List<PlatformCommissionEntry> pending = entryRepository
                .findByMerchantIdAndStatusOrderByPaidAtDesc(merchantId, PlatformCommissionEntry.EntryStatus.PENDING);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("merchantId", merchant.getId());
        map.put("merchantName", merchant.getName());
        map.put("rule", rule);
        map.put("pendingCommission", entryRepository.sumPendingByMerchant(merchantId));
        map.put("pendingEntries", pending);
        map.put("settlements", settlementRepository.findByMerchantIdOrderBySettledAtDesc(merchantId));
        return map;
    }

    @Transactional
    public PlatformCommissionSettlement settle(Long merchantId, String periodType,
                                               LocalDate periodStartDate, LocalDate periodEndDate,
                                               String voucherNo, String remark) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new BusinessException(404, "商户不存在");
        }
        PlatformCommissionSettlement.PeriodType type;
        try {
            type = PlatformCommissionSettlement.PeriodType.valueOf(periodType);
        } catch (Exception e) {
            throw new BusinessException(400, "periodType 须为 DAY/MONTH/YEAR/CUSTOM");
        }

        LocalDateTime start;
        LocalDateTime end;
        if (type == PlatformCommissionSettlement.PeriodType.CUSTOM) {
            if (periodStartDate == null || periodEndDate == null) {
                throw new BusinessException(400, "CUSTOM 须提供 periodStart 与 periodEnd（日期）");
            }
            if (!periodEndDate.isAfter(periodStartDate) && !periodEndDate.isEqual(periodStartDate)) {
                // allow same day: start of day to next day
            }
            start = periodStartDate.atStartOfDay();
            end = periodEndDate.plusDays(1).atStartOfDay(); // 右开区间
        } else if (type == PlatformCommissionSettlement.PeriodType.DAY) {
            LocalDate day = periodStartDate != null ? periodStartDate : LocalDate.now().minusDays(1);
            start = day.atStartOfDay();
            end = day.plusDays(1).atStartOfDay();
        } else if (type == PlatformCommissionSettlement.PeriodType.MONTH) {
            LocalDate month = periodStartDate != null ? periodStartDate.withDayOfMonth(1) : LocalDate.now().withDayOfMonth(1).minusMonths(1);
            start = month.atStartOfDay();
            end = month.plusMonths(1).atStartOfDay();
        } else { // YEAR
            LocalDate year = periodStartDate != null ? periodStartDate.withDayOfYear(1) : LocalDate.now().withDayOfYear(1).minusYears(1);
            start = year.atStartOfDay();
            end = year.plusYears(1).atStartOfDay();
        }

        List<PlatformCommissionEntry> entries = entryRepository.findPendingInPeriod(
                merchantId, PlatformCommissionEntry.EntryStatus.PENDING, start, end);
        if (entries.isEmpty()) {
            throw new BusinessException(400, "该账期内没有未结抽成明细");
        }

        BigDecimal orderSum = BigDecimal.ZERO;
        BigDecimal commissionSum = BigDecimal.ZERO;
        List<Map<String, Object>> snapshotItems = new ArrayList<>();
        for (PlatformCommissionEntry e : entries) {
            orderSum = orderSum.add(e.getOrderAmount());
            commissionSum = commissionSum.add(e.getCommissionAmount());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("entryId", e.getId());
            item.put("bizType", e.getBizType());
            item.put("bizId", e.getBizId());
            item.put("orderNo", e.getOrderNo());
            item.put("payMethod", e.getPayMethod());
            item.put("orderAmount", e.getOrderAmount());
            item.put("rate", e.getRate());
            item.put("commissionAmount", e.getCommissionAmount());
            item.put("paidAt", e.getPaidAt() == null ? null : e.getPaidAt().format(DT));
            snapshotItems.add(item);
        }

        User op;
        try {
            op = SecurityUtils.requireCurrentUser();
        } catch (Exception ex) {
            op = null;
        }

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("merchantId", merchantId);
        snapshot.put("periodType", type.name());
        snapshot.put("periodStart", start.format(DT));
        snapshot.put("periodEndExclusive", end.format(DT));
        snapshot.put("entryCount", entries.size());
        snapshot.put("orderAmountSum", orderSum);
        snapshot.put("commissionSum", commissionSum);
        snapshot.put("voucherNo", voucherNo);
        snapshot.put("remark", remark);
        snapshot.put("operatorId", op == null ? null : op.getId());
        snapshot.put("operatorName", op == null ? null : (op.getRealName() != null ? op.getRealName() : op.getUsername()));
        snapshot.put("settledAt", LocalDateTime.now().format(DT));
        snapshot.put("entries", snapshotItems);

        String snapshotJson;
        try {
            snapshotJson = objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new BusinessException(500, "生成结算快照失败");
        }

        PlatformCommissionSettlement settlement = new PlatformCommissionSettlement();
        settlement.setSettlementNo(nextSettlementNo());
        settlement.setMerchantId(merchantId);
        settlement.setPeriodType(type);
        settlement.setPeriodStart(start);
        settlement.setPeriodEnd(end);
        settlement.setEntryCount(entries.size());
        settlement.setOrderAmountSum(orderSum);
        settlement.setCommissionSum(commissionSum);
        settlement.setStatus(PlatformCommissionSettlement.SettlementStatus.SETTLED);
        settlement.setVoucherNo(voucherNo);
        settlement.setRemark(remark);
        settlement.setSnapshotJson(snapshotJson);
        if (op != null) {
            settlement.setOperatorId(op.getId());
            settlement.setOperatorName(op.getRealName() != null ? op.getRealName() : op.getUsername());
        }
        settlement.setSettledAt(LocalDateTime.now());
        settlement = settlementRepository.save(settlement);

        for (PlatformCommissionEntry e : entries) {
            e.setStatus(PlatformCommissionEntry.EntryStatus.SETTLED);
            e.setSettlementId(settlement.getId());
        }
        entryRepository.saveAll(entries);

        log.info("平台抽成结算完成 settlementNo={} merchantId={} sum={}",
                settlement.getSettlementNo(), merchantId, commissionSum);
        return settlement;
    }

    public PlatformCommissionSettlement getSettlement(Long id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "结算单不存在"));
    }

    public List<PlatformCommissionSettlement> listSettlements(Long merchantId) {
        if (merchantId != null) {
            return settlementRepository.findByMerchantIdOrderBySettledAtDesc(merchantId);
        }
        return settlementRepository.findTop50ByOrderBySettledAtDesc();
    }

    private String nextSettlementNo() {
        return "PCS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
    }
}
