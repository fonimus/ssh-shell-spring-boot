/*
 * Copyright (c) 2020 François Onimus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fonimus.ssh.shell.auth;

import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.server.session.ServerSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.fonimus.ssh.shell.auth.SshShellSecurityAuthenticationProvider.AUTHENTICATION_ATTRIBUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class SshShellSecurityAuthenticationProviderTest {

    private ApplicationContext ctx;

    private AuthenticationManager sec;

    @BeforeEach
    void setUp() {
        ctx = Mockito.mock(ApplicationContext.class);
        sec = Mockito.mock(AuthenticationManager.class);
    }

    @Test
    void init() {
        SshShellSecurityAuthenticationProvider provider = new SshShellSecurityAuthenticationProvider(ctx, null);
        BeanCreationException ex = assertThrows(BeanCreationException.class, provider::init);
        assertTrue(ex.getMessage().contains("find any beans of"));

        Map<String, Object> map = new HashMap<>();
        map.put("sec1", sec);
        map.put("sec2", sec);
        Mockito.when(ctx.getBeansOfType(any())).thenReturn(map);
        ex = assertThrows(BeanCreationException.class, provider::init);
        assertTrue(ex.getMessage().contains("too many beans of"));

        Mockito.when(ctx.getBeansOfType(any())).thenReturn(Collections.singletonMap("sec", sec));
        provider.init();
    }

    @Test
    void initWithBeanName() {
        ApplicationContext ctx = Mockito.mock(ApplicationContext.class);
        AuthenticationManager sec = Mockito.mock(AuthenticationManager.class);
        SshShellSecurityAuthenticationProvider provider = new SshShellSecurityAuthenticationProvider(ctx, "sec");

        Map<String, Object> map = new HashMap<>();
        map.put("sec1", sec);
        map.put("sec2", sec);
        Mockito.when(ctx.getBeansOfType(any())).thenReturn(map);
        BeanCreationException ex = assertThrows(BeanCreationException.class, provider::init);
        assertTrue(ex.getMessage().contains("not find bean with name"));

        map.put("sec", sec);
        Mockito.when(ctx.getBeansOfType(any())).thenReturn(Collections.singletonMap("sec", sec));
        provider.init();
    }

    @Test
    void authenticate() {
        ServerSession session = Mockito.mock(ServerSession.class);
        IoSession io = Mockito.mock(IoSession.class);
        Mockito.when(session.getIoSession()).thenReturn(io);
        Mockito.when(ctx.getBeansOfType(any())).thenReturn(Collections.singletonMap("sec", sec));
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        Mockito.when(io.setAttribute(eq(AUTHENTICATION_ATTRIBUTE), captor.capture())).thenReturn(null);
        SshShellSecurityAuthenticationProvider provider = new SshShellSecurityAuthenticationProvider(ctx, null);
        provider.init();

        Mockito.when(sec.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("principal", "credentials",
                        Collections.singletonList(new SimpleGrantedAuthority("USER"))));
        assertTrue(provider.authenticate("user", "pass", session));
        SshAuthentication auth = (SshAuthentication) captor.getValue();
        assertEquals("principal", auth.getPrincipal());
        assertEquals("credentials", auth.getCredentials());
        assertEquals(1, auth.getAuthorities().size());
        assertNull(auth.getDetails());

        // fail auth
        Mockito.when(sec.authenticate(any())).thenThrow(new BadCredentialsException("[MOCK]"));
        assertFalse(provider.authenticate("user", "pass", session));
    }
}
