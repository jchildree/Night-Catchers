package com.nightcatchers.feature.filters

import android.content.Context
import android.opengl.GLES30
import javax.inject.Inject
import javax.inject.Singleton

data class GlProgram(val id: Int, val layer: FilterLayer)

@Singleton
class ShaderProgramCache @Inject constructor() {

    private val cache = mutableMapOf<FilterLayer, GlProgram>()

    private val vertexShaderSource = """
        #version 300 es
        in vec4 a_Position;
        in vec2 a_TexCoord;
        out vec2 v_TexCoord;
        void main() {
            gl_Position = a_Position;
            v_TexCoord = a_TexCoord;
        }
    """.trimIndent()

    fun getOrCreate(context: Context, layer: FilterLayer): GlProgram =
        cache.getOrPut(layer) {
            val fragSrc = context.assets.open(layer.shaderAsset).bufferedReader().readText()
            val programId = compileAndLink(vertexShaderSource, fragSrc)
            GlProgram(programId, layer)
        }

    fun releaseAll() {
        cache.values.forEach { GLES30.glDeleteProgram(it.id) }
        cache.clear()
    }

    private fun compileAndLink(vertSrc: String, fragSrc: String): Int {
        val vert = compileShader(GLES30.GL_VERTEX_SHADER, vertSrc)
        val frag = compileShader(GLES30.GL_FRAGMENT_SHADER, fragSrc)
        return GLES30.glCreateProgram().also { prog ->
            GLES30.glAttachShader(prog, vert)
            GLES30.glAttachShader(prog, frag)
            GLES30.glLinkProgram(prog)
            GLES30.glDeleteShader(vert)
            GLES30.glDeleteShader(frag)
        }
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        return shader
    }
}
