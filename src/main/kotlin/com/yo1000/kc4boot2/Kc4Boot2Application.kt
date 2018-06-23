package com.yo1000.kc4boot2

import org.keycloak.adapters.AdapterDeploymentContext
import org.keycloak.adapters.springsecurity.AdapterDeploymentContextFactoryBean
import org.keycloak.adapters.springsecurity.KeycloakConfiguration
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.ClassPathResource
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
class Kc4Boot2Application

fun main(args: Array<String>) {
    runApplication<Kc4Boot2Application>(*args)
}

@KeycloakConfiguration
class SecurityConfig : KeycloakWebSecurityConfigurerAdapter() {
    override fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy = RegisterSessionAuthenticationStrategy(
            SessionRegistryImpl())

    override fun adapterDeploymentContext(): AdapterDeploymentContext = AdapterDeploymentContextFactoryBean(
            ClassPathResource("keycloak.json")).also { it.afterPropertiesSet() }.`object`!!

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth!!.authenticationProvider(keycloakAuthenticationProvider().also {
            it.setGrantedAuthoritiesMapper(SimpleAuthorityMapper().also {
                it.setConvertToUpperCase(true)
            })
        })
    }

    override fun configure(http: HttpSecurity) {
        super.configure(http)
        http.authorizeRequests()
                .antMatchers("/sso/login*").permitAll()
                .antMatchers("/customers*").hasAnyRole("USER")
                .antMatchers("/admin*").hasAnyRole("ADMIN")
                .anyRequest().permitAll();
        http.csrf().disable()
    }
}

@RestController
class Kc4Controller {
    @GetMapping("/customers")
    fun getCustomers(token: KeycloakAuthenticationToken): Any = """
        <h1>customers</h1>
        <p><code>
        ${token}
        </code></p>
        <ul>
        <li><code>${token.name}</code></li>
        <li><code>${token.account.keycloakSecurityContext.token.preferredUsername}</code></li>
        </ul>
        """.trimIndent()

    @GetMapping("/admin")
    fun getAdmin(token: KeycloakAuthenticationToken): Any = """
        <h1>admin</h1>
        <p><code>
        ${token}
        </code></p>
        <ul>
        <li><code>${token.name}</code></li>
        <li><code>${token.account.keycloakSecurityContext.token.preferredUsername}</code></li>
        </ul>
        """.trimIndent()
}