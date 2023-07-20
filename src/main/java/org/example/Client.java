package org.example;

import com.example.grpc.DownloadServiceGrpc;
import com.example.grpc.DownloadServiceOuterClass;
import com.example.grpc.GreetingServiceGrpc;
import com.example.grpc.GreetingServiceOuterClass;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class Client {
    public static void main(String[] args) {
        /**
         * Соединяемся с сервером. Создаем канал передачи данных
         */
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:1232")
                .maxInboundMessageSize(99999999)
                .usePlaintext()
                .build();

//        getMessage(channel);
        getFileDownload(channel);

        /**
         * Закрытие канала после завершения работы с ним
         */
        channel.shutdownNow();
    }

    public static void getMessage(ManagedChannel channel) {
        /**
         * Стаб - объект, на котором происходит вызов удаленного метода.
         */
        GreetingServiceGrpc.GreetingServiceBlockingStub stub =
                GreetingServiceGrpc.newBlockingStub(channel);


        /**
         * Создание запроса.
         */
        GreetingServiceOuterClass.HelloRequest request = GreetingServiceOuterClass.HelloRequest
                .newBuilder()
                .setName("Jeremy")
                .build();

        /**
         * Вызов реквеста на стабе и непосредственное получение ответа
         * Итератор используется для обеспечения потокового приема ответов.
         * Если на сервере поток, а итератора нет - будет ошибка компиляции
         */
        Iterator<GreetingServiceOuterClass.HelloResponse> response = stub.streaming(request);

        while (response.hasNext())
            System.out.println(response.next());
    }

    public static void getFileDownload(ManagedChannel channel) {
        /**
         * Стаб - объект, на котором происходит вызов удаленного метода.
         */
        DownloadServiceGrpc.DownloadServiceBlockingStub stub =
                DownloadServiceGrpc.newBlockingStub(channel);


        /**
         * Создание запроса.
         */
        DownloadServiceOuterClass.DownloadRequest request = DownloadServiceOuterClass.DownloadRequest
                .newBuilder()
                .setRequestPayload("Random_Token")
                .build();

        /**
         * Вызов реквеста на стабе и непосредственное получение ответа
         * Итератор используется для обеспечения потокового приема ответов.
         * Если на сервере поток, а итератора нет - будет ошибка компиляции
         */
        Iterator<DownloadServiceOuterClass.DownloadResponse> response = stub.downloadFile(request);

//         try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

        try (FileOutputStream out = new FileOutputStream("downloaded.tmp");
             BufferedOutputStream bos = new BufferedOutputStream(out)) {
            // перевод строки в байты

            while (response.hasNext()) {
                byte[] buffer = response.next().getFilePayload().toByteArray();
                bos.write(buffer, 0, buffer.length);
                System.out.println("Взяли часть файла");
            }
            System.out.println("Файл записан");

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }


}

