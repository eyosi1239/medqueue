FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY src/ src/

RUN mkdir -p out && javac -d out \
  src/com/medqueue/model/Doctor.java \
  src/com/medqueue/model/Appointment.java \
  src/com/medqueue/model/MedicalRecord.java \
  src/com/medqueue/model/Invoice.java \
  src/com/medqueue/server/JsonParser.java \
  src/com/medqueue/server/BaseHandler.java \
  src/com/medqueue/repository/Repository.java \
  src/com/medqueue/repository/DoctorRepository.java \
  src/com/medqueue/repository/AppointmentRepository.java \
  src/com/medqueue/repository/MedicalRecordRepository.java \
  src/com/medqueue/repository/InvoiceRepository.java \
  src/com/medqueue/repository/DataRegistry.java \
  src/com/medqueue/controller/DoctorController.java \
  src/com/medqueue/controller/AppointmentController.java \
  src/com/medqueue/controller/MedicalRecordController.java \
  src/com/medqueue/controller/InvoiceController.java \
  src/com/medqueue/controller/StatsController.java \
  src/com/medqueue/Main.java

EXPOSE 8080
CMD ["java", "-cp", "out", "com.medqueue.Main"]
