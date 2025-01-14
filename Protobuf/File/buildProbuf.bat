@echo on

start ..\Protobuf_Compiler\Windows\bin\protoc.exe --java_out=..\..\QoRManager\Shared\src\ .\CommunicationMessages.proto

start ..\Protobuf_Compiler\Windows\bin\protoc.exe --csharp_out=..\..\Collector\Collector\Communication\DataModel\ .\CommunicationMessages.proto

start ..\Protobuf_Compiler\Windows\bin\protoc.exe --csharp_out=..\..\Encapsulation\Encapsulation\Communication\DataModel\ .\CommunicationMessages.proto


