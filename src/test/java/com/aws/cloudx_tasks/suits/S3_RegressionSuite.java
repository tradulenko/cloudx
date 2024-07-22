package com.aws.cloudx_tasks.suits;


import com.aws.cloudx_tasks.s3_task.CXQA_S3_01_instatce_requirement;
import com.aws.cloudx_tasks.s3_task.CXQA_S3_02_S3_bucket_requirements;
import com.aws.cloudx_tasks.s3_task.CXQA_S3_03_04_05_06_work_with_file;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CXQA_S3_01_instatce_requirement.class,
        CXQA_S3_02_S3_bucket_requirements.class,
        CXQA_S3_03_04_05_06_work_with_file.class
})
public class S3_RegressionSuite {
}
