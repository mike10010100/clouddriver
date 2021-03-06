/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.aws.deploy.converters

import com.netflix.spinnaker.clouddriver.aws.deploy.description.DeleteAmazonLoadBalancerDescription
import com.netflix.spinnaker.clouddriver.aws.AmazonOperation
import com.netflix.spinnaker.clouddriver.aws.deploy.ops.loadbalancer.DeleteAmazonLoadBalancerClassicAtomicOperation
import com.netflix.spinnaker.clouddriver.aws.deploy.ops.loadbalancer.DeleteAmazonLoadBalancerV2AtomicOperation
import com.netflix.spinnaker.clouddriver.aws.model.AmazonLoadBalancerType
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperations
import com.netflix.spinnaker.clouddriver.security.AbstractAtomicOperationsCredentialsSupport
import org.springframework.stereotype.Component

@AmazonOperation(AtomicOperations.DELETE_LOAD_BALANCER)
@Component("deleteAmazonLoadBalancerDescription")
class DeleteAmazonLoadBalancerAtomicOperationConverter extends AbstractAtomicOperationsCredentialsSupport {
  private void sanitizeInput(Map input) {
    // default to classic load balancer if no type specified
    if (!input.containsKey("loadBalancerType")) {
      input.put("loadBalancerType", AmazonLoadBalancerType.CLASSIC.toString())
    }
  }

  @Override
  AtomicOperation convertOperation(Map input) {
    this.sanitizeInput(input)

    if (input.get("loadBalancerType").equals(AmazonLoadBalancerType.CLASSIC.toString())) {
      return new DeleteAmazonLoadBalancerClassicAtomicOperation(convertDescription(input))
    }
    return new DeleteAmazonLoadBalancerV2AtomicOperation(convertDescription(input))
  }

  @Override
  DeleteAmazonLoadBalancerDescription convertDescription(Map input) {
    this.sanitizeInput(input)
    Map<String, Object> description = new HashMap<>()
    description.putAll(input)
    description.put("loadBalancerType", AmazonLoadBalancerType.getByValue((String)description.get("loadBalancerType")));

    def converted = objectMapper.convertValue(description, DeleteAmazonLoadBalancerDescription)
    converted.credentials = getCredentialsObject(input.credentials as String)
    converted
  }
}
