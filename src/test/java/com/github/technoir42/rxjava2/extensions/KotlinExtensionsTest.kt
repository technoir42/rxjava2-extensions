package com.github.technoir42.rxjava2.extensions

import io.reactivex.Observable
import org.junit.jupiter.api.Test

class KotlinExtensionsTest {
    @Test
    fun endWith() {
        Observable.just(1, 2)
            .endWith(3)
            .test()
            .assertResult(1, 2, 3)
    }

    @Test
    fun surroundWith() {
        Observable.just(2)
            .surroundWith(1, 3)
            .test()
            .assertResult(1, 2, 3)
    }

    @Test
    fun pairwiseWithPrevious() {
        Observable.just(1, 2, 3)
            .pairwiseWithPrevious()
            .test()
            .assertResult(
                1 to null,
                2 to 1,
                3 to 2
            )
    }
}
