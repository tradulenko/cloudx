package com.aws.cloudx_tasks.suits;


import com.aws.cloudx_tasks.vpc_task.CXQA_VPC_01_check_configuration;
import com.aws.cloudx_tasks.vpc_task.CXQA_VPC_02_check_subnets__and_routing;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CXQA_VPC_01_check_configuration.class,
        CXQA_VPC_02_check_subnets__and_routing.class
})
public class VPC_RegressionSuite {
}
