package com.quorum.tessera.transaction.publish;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import org.junit.Test;

import java.util.ServiceLoader;

import static org.mockito.Mockito.*;

public class PayloadPublisherTest {

    @Test
    public void create() {
        try(
            var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class);
            var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)
        ) {

            ServiceLoader<PayloadPublisher> serviceLoader = mock(ServiceLoader.class);
            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(PayloadPublisher.class)).thenReturn(serviceLoader);

            PayloadPublisher.create();

            serviceLoaderUtilMockedStatic.verify(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
            serviceLoaderUtilMockedStatic.verifyNoMoreInteractions();

            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(PayloadPublisher.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();
            verifyNoInteractions(serviceLoader);
        }
    }


}
