package com.duoc.advanced;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Clase que se encarga de ejecutar el trabajo de Spring Batch llamado "ventasJob".
 * Utiliza el JobLauncher para iniciar el trabajo con parámetros únicos.
 */
@Component
public class JobRunner {

    @Autowired
    private JobLauncher jobLauncher; // Lanzador del trabajo, encargado de ejecutar los jobs

    @Autowired
    private Job ventasJob; // Trabajo de ventas configurado en Spring Batch

    /**
     * Método que ejecuta el trabajo "ventasJob" con parámetros únicos para cada ejecución.
     * Los parámetros se establecen para garantizar que cada ejecución sea reconocida como única por Spring Batch.
     */
    public void runVentasJob() {
        try {
            // Configura los parámetros para la ejecución del job
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // Agrega un parámetro de tiempo único
                    .toJobParameters();
            
            // Ejecuta el trabajo de ventas con los parámetros configurados
            jobLauncher.run(ventasJob, jobParameters);
        } catch (Exception e) {
            e.printStackTrace(); // Manejo básico de excepciones en caso de fallo en la ejecución del job
        }
    }
}
