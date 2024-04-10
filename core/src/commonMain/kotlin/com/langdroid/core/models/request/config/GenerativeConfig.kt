package com.langdroid.core.models.request.config

import com.langdroid.core.models.GenerativeModel

public interface GenerativeConfig<M : GenerativeModel> {
    public val temperature: Float?
    public val topP: Float?
    public val topK: Int?
    public val maxOutputTokens: Int?


    public companion object {
        public inline fun <reified M : GenerativeModel> create(onCreate: Builder<M>.() -> Unit = {}): GenerativeConfig<M> {
//            val requestConfigFields = when (M::class) {
//                OpenAiModel::class -> OpenAiRequestConfigFields()
//                GeminiModel::class -> GeminiRequestConfigFields()
//                else -> DefaultRequestConfigFields()
//            }

            val builder = Builder<M>()
            onCreate(builder)
            return builder.build()
        }
    }

    public class Builder<M : GenerativeModel> {
        public var temperature: Float? = null
        public var topP: Float? = null
        public var topK: Int? = null
        public var maxOutputTokens: Int? = null


        public fun temperature(temperature: Float): Builder<M> =
            apply { this.temperature = temperature }

        public fun topP(topP: Float): Builder<M> = apply { this.topP = topP }
        public fun topK(topK: Int): Builder<M> = apply { this.topK = topK }
        public fun maxOutputTokens(maxOutputTokens: Int): Builder<M> =
            apply { this.maxOutputTokens = maxOutputTokens }

        public fun build(): GenerativeConfig<M> = object : GenerativeConfig<M> {
            override val temperature: Float? = this@Builder.temperature
            override val topP: Float? = this@Builder.topP
            override val topK: Int? = this@Builder.topK
            override val maxOutputTokens: Int? = this@Builder.maxOutputTokens
        }
    }
}
