package com.sportvenue.venue.dto.merchant;

import lombok.Data;

@Data
public class WxChannelUpsertRequest {
    /** MINI_PROGRAM / OFFICIAL_ACCOUNT */
    private String channelType;
    private String appId;
    /** 明文 secret，仅写入；不回显 */
    private String appSecret;
    private String oaServerToken;
    private String oaEncodingAesKey;
    private String remark;
}
