/*******************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.sp.internal;

import com.wso2telco.core.pcrservice.PCRGeneratable;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.apache.felix.scr.annotations.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Component(name = "com.wso2telco.internal.SpProvisionPcrServiceComponent", immediate = true)
@Reference(
        name = "com.wso2telco.core.pcrservice.internal.PCRServiceComponent",
        referenceInterface = com.wso2telco.core.pcrservice.PCRGeneratable.class,
        cardinality = ReferenceCardinality.MANDATORY_UNARY,
        policy = ReferencePolicy.DYNAMIC,
        bind = "setPcrService",
        unbind = "unsetPcrService"
)
public class SpProvisionPcrServiceComponent {

    private static Log log = LogFactory.getLog(SpProvisionPcrServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        log.debug("SpProvision Bundle Activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    protected void setPcrService(PCRGeneratable pcrGeneratable) {
        SpProvisionPcrDataHolder.getInstance().setPcrGeneratable(pcrGeneratable);
    }

    protected void unsetPcrService(PCRGeneratable pcrGeneratable) {
        SpProvisionPcrDataHolder.getInstance().setPcrGeneratable(null);
    }
}
