package com.nightcatchers.feature.filters

import android.opengl.GLES30
import javax.inject.Inject

class GlslUniformBinder @Inject constructor() {

    fun bindFloat(programId: Int, name: String, value: Float) {
        val loc = GLES30.glGetUniformLocation(programId, name)
        if (loc >= 0) GLES30.glUniform1f(loc, value)
    }

    fun bindInt(programId: Int, name: String, value: Int) {
        val loc = GLES30.glGetUniformLocation(programId, name)
        if (loc >= 0) GLES30.glUniform1i(loc, value)
    }

    fun bindVec2(programId: Int, name: String, x: Float, y: Float) {
        val loc = GLES30.glGetUniformLocation(programId, name)
        if (loc >= 0) GLES30.glUniform2f(loc, x, y)
    }

    fun bindVec4(programId: Int, name: String, r: Float, g: Float, b: Float, a: Float) {
        val loc = GLES30.glGetUniformLocation(programId, name)
        if (loc >= 0) GLES30.glUniform4f(loc, r, g, b, a)
    }
}
