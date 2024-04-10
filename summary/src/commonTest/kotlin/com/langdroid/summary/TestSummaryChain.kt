package com.langdroid.summary

import app.cash.turbine.test
import com.langdroid.core.LangDroidModel
import com.langdroid.summary.Mocks.MOCK_TEXT
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestSummaryChain {

    // Way to check if returned states are correct
    private fun runSummaryChainTest(
        description: String,
        isStream: Boolean,
        setup: suspend TestScope.() -> Pair<LangDroidModel<*>, List<SummaryState>>
    ) = runTest {
        val (model, expectedStates) = setup()

        val summaryChain = SummaryChain(model, isStream, coroutineScope = backgroundScope)

        val isCompletenessTest = checkIsCompletenessTest(description)

        val testFlow = if (isCompletenessTest) {
            summaryChain.invokeAndGetFlow(MOCK_TEXT).takeWhile { !it.isFinished() }
        } else summaryChain.invokeAndGetFlow(MOCK_TEXT)

        testFlow.test {
            for (state in expectedStates) {
                assertEquals(state, awaitItem())
            }
            if (isCompletenessTest) awaitComplete()
        }
    }

    // Scenarios with isStream = false
    @Test
    fun testSummariesWithStreamFalse() {
        runSummaryChainTest("Normal", false) {
            Mocks.createModelNormalWithList(isStream = false)
        }
    }

    @Test
    fun testSummariesCompleteWithStreamFalse() {
        runSummaryChainTest("Completeness Test", false) {
            Mocks.createModelNormalWithList(isStream = false, isTestCompleteness = true)
        }
    }

    @Test
    fun testSummariesReduceWithStreamFalse() {
        runSummaryChainTest("Reduce Text Test", false) {
            Mocks.createModelNormalWithList(isStream = false, isReduceText = true)
        }
    }

    // Scenarios with isStream = true
    @Test
    fun testSummariesWithStreamTrue() {
        runSummaryChainTest("Normal", true) {
            Mocks.createModelNormalWithList(isStream = true)
        }
    }

    @Test
    fun testSummariesCompleteWithStreamTrue() {
        runSummaryChainTest("Completeness Test", true) {
            Mocks.createModelNormalWithList(isStream = true, isTestCompleteness = true)
        }
    }

    @Test
    fun testSummariesReduceWithStreamTrue() {
        runSummaryChainTest("Reduce Text Test", true) {
            Mocks.createModelNormalWithList(isStream = true, isReduceText = true)
        }
    }


    private fun checkIsCompletenessTest(description: String) =
        description.contains("complete", true)
}
