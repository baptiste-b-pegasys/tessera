package net.consensys.tessera.migration.data;

import com.quorum.tessera.data.PrivacyGroupEntity;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyGroupUtil;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PersistPrivacyGroupEventHandlerTest {

    private PersistPrivacyGroupEventHandler persistPrivacyGroupEventHandler;

    private EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    @Before
    public void beforeTest() {

        entityManagerFactory = mock(EntityManagerFactory.class);
        entityManager = mock(EntityManager.class);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);

        persistPrivacyGroupEventHandler = new PersistPrivacyGroupEventHandler(entityManagerFactory);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(entityManagerFactory);
        verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void handle() throws Exception {

        OrionEvent orionEvent = mock(OrionEvent.class);
        when(orionEvent.getPayloadType()).thenReturn(PayloadType.PRIVACY_GROUP_PAYLOAD);
        when(orionEvent.getKey()).thenReturn("privacyGroupId".getBytes());

        List<String> addresses = List.of(
            "arhIcNa+MuYXZabmzJD5B33F3dZgqb0hEbM3FZsylSg=",
            "B687sgdtqsem2qEXO8h8UqvW1Mb3yKo7id5hPFLwCmY=");

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("addresses",Json.createArrayBuilder(addresses))
            .add("description","A very private group")
            .add("name","Give me lard")
            .add("state", "ACTIVE")
            .add("type","LEGACY")
            .add("randomSeed","lYrcf8bPzEGTl1eY9HxAZla8qCI=")
            .build();

        when(orionEvent.getJsonObject()).thenReturn(jsonObject);

        ArgumentCaptor<PrivacyGroupEntity> persistArgCapture
            = ArgumentCaptor.forClass(PrivacyGroupEntity.class);

        persistPrivacyGroupEventHandler.onEvent(orionEvent);

        verify(entityManagerFactory).createEntityManager();
        verify(entityManager).persist(persistArgCapture.capture());

        PrivacyGroupEntity result = persistArgCapture.getValue();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getTimestamp()).isNotNull();

        PrivacyGroup privacyGroup = PrivacyGroupUtil.create().decode(result.getData());

        assertThat(privacyGroup.getDescription()).isEqualTo(jsonObject.getString("description"));
        assertThat(privacyGroup.getName()).isEqualTo(jsonObject.getString("name"));
        assertThat(privacyGroup.getState()).isEqualTo(PrivacyGroup.State.ACTIVE);
        assertThat(privacyGroup.getType()).isEqualTo(PrivacyGroup.Type.LEGACY);
        assertThat(privacyGroup.getSeed()).isEqualTo(Base64.getDecoder().decode(jsonObject.getString("randomSeed")));

        assertThat(privacyGroup.getMembers()).hasSize(2);

        assertThat(privacyGroup.getMembers().stream()
            .map(PublicKey::encodeToBase64)
            .collect(Collectors.toList())
        ).containsExactlyElementsOf(addresses);

    }

    @Test
    public void ignoreNonEncryptedPayloadTypes() throws Exception {
        Stream<PayloadType> payloadTypes = Stream.of(PayloadType.ENCRYPTED_PAYLOAD,PayloadType.QUERY_PRIVACY_GROUP_PAYLOAD);
        List<OrionEvent> events = payloadTypes.map(payloadType -> {
            OrionEvent orionEvent = mock(OrionEvent.class);
            when(orionEvent.getPayloadType()).thenReturn(payloadType);
            return orionEvent;
        }).collect(Collectors.toList());

        for(OrionEvent orionEvent : events) {
            persistPrivacyGroupEventHandler.onEvent(orionEvent);
        }

        verifyNoInteractions(entityManagerFactory);
    }


}
