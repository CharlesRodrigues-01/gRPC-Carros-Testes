package br.com.zupacademy

import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CarrosEndpointTest(val repository: CarroRepository,
                                  val grpcClient : CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub){

    @BeforeEach
    fun setUp(){
        repository.deleteAll()
    }

    @Test
    fun `deve adicionar um novo carro`(){

        // cenário

        // ação
        val response = grpcClient.adicionar(CarrosRequest
            .newBuilder()
            .setModelo("Gol")
            .setPlaca("EWE-1234")
            .build())

        // validação
        with(response) {
            assertNotNull(this.id)
            assertTrue(repository.existsById(this.id)) // efeito colateral
        }

    }

    @Test
    fun `não deve adicionar novo carro com mesma placa`(){
        // cenário
        val existente = repository.save(Carro("Corsa", "RSS-9088"))

        // ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(CarrosRequest
                .newBuilder()
                .setModelo("Ferrari")
                .setPlaca(existente.placa)
                .build())
        }

        // validação
        with(error){
            assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
            assertEquals("carro com placa existente", this.status.description)
        }
    }

    @Test
    fun `não deve adicionar novo carro quando dados forem invalidos`(){
        // cenário

        // ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(CarrosRequest
                .newBuilder()
                .setModelo("")
                .setPlaca("")
                .build())
        }

        // validação
        with(error){
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("dados de entrada inválidos", this.status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) : CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub?{
            return CarrosGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

}