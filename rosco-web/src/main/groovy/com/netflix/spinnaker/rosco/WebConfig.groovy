/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.rosco

import com.netflix.spectator.api.Registry
import com.netflix.spinnaker.filters.AuthenticatedRequestFilter
import com.netflix.spinnaker.kork.web.interceptors.MetricsInterceptor
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.util.StringUtils
import org.springframework.web.servlet.config.annotation.*


@Configuration
@CompileStatic
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

  @Autowired
  Registry registry

  private final String baseUrl

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(
            new MetricsInterceptor(
                    this.registry, "controller.invocations", ["cloudProvider", "region"], ["BasicErrorController"]
            )
    )
  }

  @Bean
  AuthenticatedRequestFilter authenticatedRequestFilter() {
    return new AuthenticatedRequestFilter(true);
  }

  @Bean
  FilterRegistrationBean authenticatedRequestFilterRegistrationBean(AuthenticatedRequestFilter authenticatedRequestFilter) {
    def frb = new FilterRegistrationBean(authenticatedRequestFilter);
    frb.order = Ordered.HIGHEST_PRECEDENCE
    return frb
  }

/*
 * TODO : CVE fixes:12Apr23:Sheetal:Need to check and remove addResourceHandlers() and addViewControllers() methods, if they are not getting used from here.
*/
@Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String baseUrl = StringUtils.trimTrailingCharacter(this.baseUrl, '/' as char);
    registry.
            addResourceHandler(baseUrl + "/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
            .resourceChain(false);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController(baseUrl + "/swagger-ui/")
            .setViewName("forward:" + baseUrl + "/swagger-ui/index.html");
  }

}

