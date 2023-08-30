package com.mildp.jetpackcompose.utils

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*

class ProgressRequestBody (
    private val requestBody: RequestBody,
    private val progressListener: (Long, Long) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = countingSink.buffer()

        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
        private var bytesWritten = 0L

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            progressListener(bytesWritten, contentLength())
        }
    }
}