package br.com.zupacademy

import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
data class Carro(
    @field:NotBlank
    @field:Column(nullable = false)
    val modelo: String,

    @field:NotBlank
    @field:Column(nullable = false)
    val placa: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
