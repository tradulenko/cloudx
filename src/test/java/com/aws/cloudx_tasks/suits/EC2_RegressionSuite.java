package com.aws.cloudx_tasks.suits;


import com.aws.cloudx_tasks.ec2_task.CXQA_EC2_01_check_types;
import com.aws.cloudx_tasks.ec2_task.CXQA_EC2_02_check_instance_configuration;
import com.aws.cloudx_tasks.ec2_task.CXQA_EC2_03_check_security_groups_config;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CXQA_EC2_01_check_types.class,
        CXQA_EC2_02_check_instance_configuration.class,
        CXQA_EC2_03_check_security_groups_config.class
})
public class EC2_RegressionSuite {
}
