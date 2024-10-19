package com.zioinnovate.calculadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private val inputBuffer = StringBuilder()
    private var resultado: Double = 0.0
    private var resultadoPronto: Boolean = false
    private var valorInicial: Double = 0.0
    private var operador: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultTextView = findViewById(R.id.resultTextView)

        val botoesNumeros = listOf(
            R.id.button0, R.id.button1, R.id.button2, R.id.button3,
            R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9
        )

        botoesNumeros.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                adicionarNumero((it as Button).text.toString())
            }
        }

        findViewById<Button>(R.id.buttonSomar).setOnClickListener { adicionarOperador("+") }
        findViewById<Button>(R.id.buttonSubtrair).setOnClickListener { adicionarOperador("-") }
        findViewById<Button>(R.id.buttonMultiplicar).setOnClickListener { adicionarOperador("*") }
        findViewById<Button>(R.id.buttonDividir).setOnClickListener { adicionarOperador("/") }

        findViewById<Button>(R.id.buttonIgual).setOnClickListener { calcularResultado() }

        findViewById<Button>(R.id.buttonLimpar).setOnClickListener { limpar() }

        findViewById<Button>(R.id.buttonBackSpace).setOnClickListener { apagarUltimoDigito() }

        findViewById<Button>(R.id.buttonPorcentagem).setOnClickListener { calcularPorcentagem() }

        findViewById<Button>(R.id.buttonNegativo).setOnClickListener { trocarSinal() }

        findViewById<Button>(R.id.buttonPonto).setOnClickListener { adicionarPonto() }
    }

    private fun adicionarNumero(numero: String) {
        if (resultadoPronto) {
            inputBuffer.setLength(0)
            resultadoPronto = false
        }
        inputBuffer.append(numero)
        atualizarVisor()
    }

    private fun adicionarPonto() {
        if (inputBuffer.isEmpty() || inputBuffer.contains(".")) {
            if (inputBuffer.isNotEmpty() && !isOperador(inputBuffer.last())) {
                return
            }
        }
        inputBuffer.append(".")
        atualizarVisor()
    }

    private fun limpar() {
        inputBuffer.setLength(0)
        resultado = 0.0
        valorInicial = 0.0
        atualizarVisor()
    }

    private fun apagarUltimoDigito() {
        if (inputBuffer.isNotEmpty()) {
            inputBuffer.deleteCharAt(inputBuffer.length - 1)
            atualizarVisor()
        }
    }

    private fun adicionarOperador(op: String) {
        if (resultadoPronto) {
            inputBuffer.setLength(0)
            inputBuffer.append(resultado)
            resultadoPronto = false
        }

        if (inputBuffer.isNotEmpty() && !isOperador(inputBuffer.last())) {
            inputBuffer.append(op)
            atualizarVisor()
        }
    }

    private fun calcularResultado() {
        if (inputBuffer.isNotEmpty()) {
            try {
                if (resultadoPronto) {
                    resultado += valorInicial
                } else {
                    valorInicial = evaluate(inputBuffer.toString())
                    resultado = valorInicial
                    resultadoPronto = true
                }
                atualizarVisorComResultado()
            } catch (e: Exception) {
                resultTextView.text = "Erro: Entrada inválida"
            }
        }
    }

    private fun calcularPorcentagem() {
        if (inputBuffer.isNotEmpty()) {
            val partes = inputBuffer.split("+").map { it.trim() }
            if (partes.size == 2) {
                try {
                    val primeiroNumero = partes[0].toDouble()
                    val segundoNumero = partes[1].toDouble()
                    val porcentagem = segundoNumero * 0.01
                    val resultadoPorcentagem = primeiroNumero * porcentagem
                    val resultadoFinal = primeiroNumero + resultadoPorcentagem

                    resultTextView.text = String.format(
                        "%.2f + %.2f%% = %.2f",
                        primeiroNumero,
                        segundoNumero,
                        resultadoFinal
                    )
                    inputBuffer.setLength(0)
                    inputBuffer.append(resultadoFinal)
                } catch (e: NumberFormatException) {
                    resultTextView.text = "Erro: Entrada inválida"
                }
            } else {
                resultTextView.text = "Erro: Entrada inválida"
            }
        } else {
            resultTextView.text = "Erro: Entrada inválida"
        }
    }

    private fun trocarSinal() {
        if (inputBuffer.isNotEmpty()) {
            if (isOperador(inputBuffer.last())) {
                return
            }

            if (inputBuffer.toString().startsWith("-")) {
                inputBuffer.deleteCharAt(0)
            } else {
                inputBuffer.insert(0, "-")
            }
            atualizarVisor()
        }
    }

    private fun atualizarVisor() {
        resultTextView.text = inputBuffer.toString()
    }

    private fun atualizarVisorComResultado() {
        if (resultado == resultado.toLong().toDouble()) {
            resultTextView.text = String.format("%d", resultado.toLong())
        } else {
            resultTextView.text = resultado.toString()
        }
    }

    private fun isOperador(c: Char): Boolean {
        return c in arrayOf('+', '-', '*', '/')
    }

    private fun evaluate(expression: String): Double {
        return object : Any() {
            var pos: Int = -1
            var ch: Int = 0

            fun nextChar() {
                ch = if ((++pos < expression.length)) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Caractere inesperado: ${ch.toChar()}")
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor()
                    else if (eat('/'.code)) x /= parseFactor()
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = this.pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, this.pos).toDouble()
                } else {
                    throw RuntimeException("Número esperado")
                }

                if (eat('^'.code)) x = x.pow(parseFactor())
                return x
            }
        }.parse()
    }
}