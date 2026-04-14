package com.membertransfer.e2e.model.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "restResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberTransferResponse {
    @JacksonXmlProperty(localName = "status")
    private Status status;

    @JacksonXmlProperty(localName = "request")
    private Request request;

    @Data
    @JacksonXmlRootElement(localName = "status")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        @JacksonXmlProperty(localName = "message")
        private String message;

        @JacksonXmlProperty(localName = "count")
        private int count;

        @JacksonXmlProperty(localName = "messageCode")
        private String messageCode;
    }

    @Data
    @JacksonXmlRootElement(localName = "request")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {
        @JacksonXmlProperty(localName = "toClubNumber")
        private int toClubNumber;
    }
}
