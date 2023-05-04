package com.netflix.spinnaker.rosco.jobs.local

import com.netflix.spectator.api.DefaultRegistry
import com.netflix.spinnaker.rosco.api.BakeStatus
import com.netflix.spinnaker.rosco.jobs.JobRequest
import com.netflix.spinnaker.rosco.providers.util.TestDefaults
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class JobExecutorLocalSpec extends Specification implements TestDefaults {

  private static final String BASH_SCRIPT = '''\
    for i in {1..5}; do
        echo "Output $i"
        sleep 0.1
        echo "Error $i" >&2
        sleep 0.1
    done
    echo "Final output"
  '''.stripIndent()

  private static final String EXPECTED_OUTPUT = "Output 1\nOutput 2\nOutput 3\nOutput 4\nOutput 5\nFinal output\n"
  private static final String EXPECTED_LOGS = "Error 1\nError 2\nError 3\nError 4\nError 5\n"
  private static final String COMBINED_OUTPUT = "Output 1\nError 1\nOutput 2\nError 2\nOutput 3\nError 3\nOutput 4\nError 4\nOutput 5\nError 5\nFinal output\n"

  @Unroll
  void 'job executor runs command and captures stdout and stderr with combineStdOutAndErr set to #combineStdOutAndErr'() {
    setup:
      def jobRequest = new JobRequest(
          tokenizedCommand: ["/bin/bash", "-c", BASH_SCRIPT],
          jobId: SOME_JOB_ID,
          combineStdOutAndErr: combineStdOutAndErr)

      @Subject
      def jobExecutorLocal = new JobExecutorLocal(
          registry: new DefaultRegistry(),
          timeoutMinutes: 1)

    when:
      def jobId = jobExecutorLocal.startJob(jobRequest)
      // Give the script time to run + 100 ms fudge factor
      sleep(3000)
      def bakeStatus = jobExecutorLocal.updateJob(jobId)

    then:
      bakeStatus != null
      bakeStatus.state == BakeStatus.State.COMPLETED
      bakeStatus.result == BakeStatus.Result.SUCCESS
      bakeStatus.outputContent == expectedOutput
      bakeStatus.logsContent == expectedLogs

    where:
      combineStdOutAndErr | expectedOutput  | expectedLogs
      true                | COMBINED_OUTPUT | COMBINED_OUTPUT
      false               | EXPECTED_OUTPUT | EXPECTED_LOGS
  }
}
