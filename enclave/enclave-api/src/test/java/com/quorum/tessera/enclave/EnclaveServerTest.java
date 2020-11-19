package com.quorum.tessera.enclave;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import org.junit.Test;

import java.util.ServiceLoader;

import static org.mockito.Mockito.*;

public class EnclaveServerTest {

    @Test
    public void create() {
        try (
            var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class);
            var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)
        ) {

            ServiceLoader<EnclaveServer> serviceLoader = mock(ServiceLoader.class);
            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(EnclaveServer.class)).thenReturn(serviceLoader);

            EnclaveServer.create();

            serviceLoaderUtilMockedStatic.verify(() -> ServiceLoaderUtil.loadSingle(serviceLoader));
            serviceLoaderUtilMockedStatic.verifyNoMoreInteractions();

            serviceLoaderMockedStatic.verify(() -> ServiceLoader.load(EnclaveServer.class));
            serviceLoaderMockedStatic.verifyNoMoreInteractions();
            verifyNoInteractions(serviceLoader);
        }
    }

}



