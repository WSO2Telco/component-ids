/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
 ******************************************************************************/

package com.wso2telco.ids.datapublisher.internal;

import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;

import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

@Component(name = "com.wso2telco.ids.datapublisher.internal.DataPublisherServiceComponent",
           immediate = true)
@Reference(
        name = "application.mgt.service",
        referenceInterface = ApplicationManagementService.class,
        cardinality = ReferenceCardinality.OPTIONAL_UNARY,
        policy = ReferencePolicy.DYNAMIC,
        bind = "setApplicationManagementService",
        unbind = "unsetApplicationManagementService"
)
public class DataPublisherServiceComponent {

    protected void activate(ComponentContext context) {
    }

    protected void deactivate(ComponentContext context) {
    }

    protected void setApplicationManagementService(ApplicationManagementService applicationManagementService) {
        DataPublisherUtil.setApplicationManagementService(applicationManagementService);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService applicationManagementService) {
        DataPublisherUtil.setApplicationManagementService(null);
    }


}
