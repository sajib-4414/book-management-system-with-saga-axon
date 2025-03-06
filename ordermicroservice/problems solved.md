## axon server connection failed:
* have the lates axon server dependency
* when u start the axon server, go to browser for the first time before starting spring boot,
click the start single node or something to go to the dashboard. it ensures the axon is fully 
initialized and ready to connect to spring boot.
* now start the spring boot.
* if you have changed the axon server port, then change it itn the spring boot too.


## Error related to event not being able to read in other microservice, or command not being able to read
- It is due to permission error. for development, just add the axonconfig in all participating microservices, that allows to read all messages. has to be corrected later
