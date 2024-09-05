package com.vigilonix.jaanch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vigilonix.jaanch.model.ContactMessage;
import com.vigilonix.jaanch.pojo.whatsapp.WebhookPayload;
import com.vigilonix.jaanch.pojo.whatsapp.WhatsAppWebhookPayload;
import com.vigilonix.jaanch.repository.ContactMessageRepository;
import com.vigilonix.jaanch.request.OAuth2Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Map;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AnonService {
    private final ObjectMapper objectMapper;
    private final ContactMessageRepository contactMessageRepository;
    /*
    2024-09-02 09:45:51.338 [qtp1543871080-21] INFO  c.v.jaanch.service.AnonService - wba chat webhook received {object=whatsapp_business_account, entry=[{id=421686857686397, changes=[{value={messaging_product=whatsapp, metadata={display_phone_number=918986139192, phone_number_id=394097860454344}, contacts=[{profile={name=Nischal}, wa_id=919916488861}], messages=[{from=919916488861, id=wamid.HBgMOTE5OTE2NDg4ODYxFQIAEhgUM0FEMzVGOTg3OTFGNjU5QTk4RTgA, timestamp=1725270349, text={body=Hello}, type=text}]}, field=messages}]}]}

    2024-09-02 09:47:40.523 [qtp1543871080-23] INFO  c.v.jaanch.service.AnonService - wba chat webhook received {object=whatsapp_business_account, entry=[{id=421686857686397, changes=[{value={messaging_product=whatsapp, metadata={display_phone_number=918986139192, phone_number_id=394097860454344}, contacts=[{profile={name=Nischal}, wa_id=919916488861}], messages=[{context={from=918986139192, id=wamid.HBgMOTE5OTE2NDg4ODYxFQIAERgSQzMzMjQzMEJDNjQzRDIwMTMzAA==}, from=919916488861, id=wamid.HBgMOTE5OTE2NDg4ODYxFQIAEhgUM0ExRjMyNkFEMUE5OTNFNEY0MDUA, timestamp=1725270458, type=interactive, interactive={type=nfm_reply, nfm_reply={response_json={"screen_0_delivery_1":"0_⏱️_0-15_mins","screen_0_TextArea_3":"Overall a happy happy experience","flow_token":"unused","screen_0_cs_2":"4_★☆☆☆☆_•_Unsatisfactory_(1\/5)","screen_0_purchase_0":"1_★★★★☆_•_Good_(4\/5)"}, body=Sent, name=flow}}}]}, field=messages}]}]}







     */
    public void wbaChatWebhook(String payload) {
        try {
            log.info("wba chat webhook received {}", payload);
            // Deserialize JSON to WebhookPayload POJO
            WebhookPayload webhookPayload = objectMapper.readValue(payload, WebhookPayload.class);

            // Extract the first entry (assuming there's only one)
            WebhookPayload.Entry entry = webhookPayload.getEntry().get(0);
            WebhookPayload.Change change = entry.getChanges().get(0);
            WebhookPayload.Value value = change.getValue();
            String responseJson = value.getMessages().get(0).getInteractive() != null ? value.getMessages().get(0).getInteractive().getNfm_reply().getResponse_json() : objectMapper.createObjectNode().toString();
            Map<String, Object> responseJsonMap = objectMapper.readValue(responseJson, new TypeReference<Map<String, Object>>() {});



            // Create ContactMessage object using builder and map directly from POJO fields
            ContactMessage contactMessage = ContactMessage.builder()
                    .entryId(entry.getId())
                    .contactName(value.getContacts().get(0).getProfile().getName())
                    .contactWaId(value.getContacts().get(0).getWa_id())
                    .contactPhoneNumber(value.getMetadata().getDisplay_phone_number())
                    .messageId(value.getMessages().get(0).getId())
                    .messageTimestamp(new Timestamp(Long.parseLong(value.getMessages().get(0).getTimestamp()) * 1000L))
                    .messageBody(value.getMessages().get(0).getText() != null ? value.getMessages().get(0).getText().getBody() : null)
                    .messageType(value.getMessages().get(0).getType())
                    .interactiveType(value.getMessages().get(0).getInteractive() != null ? value.getMessages().get(0).getInteractive().getType() : null)
                    .raw_json(payload)
                    .interactiveResponse(responseJsonMap)
                    .metadataDisplayPhoneNumber(value.getMetadata().getDisplay_phone_number())
                    .metadataPhoneNumberId(value.getMetadata().getPhone_number_id())
                    .contextMessageId(value.getMessages().get(0).getContext() != null ? value.getMessages().get(0).getContext().getId() : null)
                    .contextFrom(value.getMessages().get(0).getContext() != null ? value.getMessages().get(0).getContext().getFrom() : null)
                    .build();
            contactMessageRepository.save(contactMessage);
        }catch (Exception e) {
            log.error("failed to parse jsonInput for chat webhook", e);
        }
    }
}
