# A Framework for Scalable Real-Time Anomaly Detection Over Voluminous, Geospatial Data Streams 
Developed is a configurable framework for anomaly detection over continuous data streams. Since anomalies evolve over time, the framework addresses the online adaptation of models in its anomaly detector interfaces. Our design allows for domain-specific behavior to handle changes in the data that must be treated as normal rather than anomalous, a feature that can be toggled at run time. 

To deal with spatial properties in the dataset, we partition incoming streams on the basis of geographical extents. We then initialize an instance of our anomaly detection model for the geographic regions, each of which continually adapts on the basis of observations recorded for the region. Depending on the data volumes involved and the number of resources available within the system, both geographic boundaries and the number of model instances can be tuned appropriately. 

The framework can be used on its own or integrated with other frameworks (such as distributed storage framework). In both cases, an instance of the framework needs to be initialized on each machine in the cluster. Each instance of the framework is responsible to partition incoming streams into geographical regions, uses a thread-pool to currently train models for the geographical regions, and employs the appropriate models to make predictions for the incoming observations. 
This ensures that the anomaly detection system is decentralized, scalable, and capable of achieving high throughput. 

## System Architecture
The implementation of the framework includes two components, a **coordinator** and **anomaly detectors**. The framework creates one coordinator and keeps forwarding the streamed observations to it. 

The **coordinator** receives observations and then forwards them to the appropriate detector instances based on the spatial partitioning scheme in use. If a detector instance for the region does not exist, the coordinator creates a new instance. The coordinator uses an abstract to load and manage detectors that can use different detection algorithms. This makes it possible to add, manage, and combine different anomaly detection techniques concurrently. Also, because of the loose coupling between the coordinator and detectors, any changes made to either component will not require corresponding changes in the other. 
Our approach ensures that available cores and execution pipelines are used efficiently by managing a thread pool to facilitate parallel detection activities. During initialization, the coordinator creates a thread pool of a configurable size and provides its reference to the anomaly detectors. Detector instances submit classification tasks that contain a queue of incoming observations to be processed concurrently, and the queues can be updated as more observations are assimilated.


The primary concern of the **anomaly detector** is to collect training data from the coordinator, build a model for the received data that cover a finer-grained geospatial scope, and then use the model to detect observations whose behaviors are outside the observed norm. Collecting the training data and training a model is done automatically for each detector regardless of the actual implementation of the anomaly detector instances. Each anomaly detector operates in three phases. It starts in the data collection phase, where the detector collects observations in memory and transitions to the training phase when the amount of data collected reaches a configurable threshold. The coordinator can also override the threshold to begin training immediately if the particular problem warrants such an action. In the training phase, a training task is created and queued to the thread pool. While the training task is running in a separate thread, observations are buffered for classification until the training process is complete. Finally, in the classification stage, the models are used to classify incoming data. 

### Figures
[anomalyDetector Component](https://github.com/wbudgaga/Anomaly-Detection-Framework/files/4866025/anomalyDetector.pdf)

## Execution

