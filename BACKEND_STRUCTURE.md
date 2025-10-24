Folder PATH listing
Volume serial number is BEF9-60AA
C:.
|   Back_end_Structure.txt
|   mvnw
|   mvnw.cmd
|   pom.xml
|   PROJECT_STRUCTURE.md
|   
+---.idea
|       .gitignore
|       compiler.xml
|       encodings.xml
|       jarRepositories.xml
|       misc.xml
|       vcs.xml
|       workspace.xml
|       
+---.mvn
|   \---wrapper
|           maven-wrapper.properties
|           
+---src
|   +---main
|   |   +---java
|   |   |   \---vn
|   |   |       \---team9
|   |   |           \---auction_system
|   |   |               |   AuctionSystemApplication.java
|   |   |               |   
|   |   |               +---auction
|   |   |               |   +---controller
|   |   |               |   |       AuctionController.java
|   |   |               |   |       
|   |   |               |   +---model
|   |   |               |   |       Auction.java
|   |   |               |   |       Bid.java
|   |   |               |   |       
|   |   |               |   +---repository
|   |   |               |   |       AuctionRepository.java
|   |   |               |   |       BidRepository.java
|   |   |               |   |       
|   |   |               |   \---service
|   |   |               |           AuctionService.java
|   |   |               |           
|   |   |               +---config
|   |   |               |       SecurityConfig.java
|   |   |               |       
|   |   |               +---feedback
|   |   |               |   +---controller
|   |   |               |   |       AdminLogController.java
|   |   |               |   |       FeedbackController.java
|   |   |               |   |       NotificationController.java
|   |   |               |   |       
|   |   |               |   +---model
|   |   |               |   |       AdminLog.java
|   |   |               |   |       Feedback.java
|   |   |               |   |       Notification.java
|   |   |               |   |       
|   |   |               |   +---repository
|   |   |               |   |       AdminLogRepository.java
|   |   |               |   |       FeedbackRepository.java
|   |   |               |   |       NotificationRepository.java
|   |   |               |   |       
|   |   |               |   \---service
|   |   |               |           AdminLogService.java
|   |   |               |           FeedbackService.java
|   |   |               |           NotificationService.java
|   |   |               |           
|   |   |               +---product
|   |   |               |   +---controller
|   |   |               |   |       ProductController.java
|   |   |               |   |       
|   |   |               |   +---model
|   |   |               |   |       Image.java
|   |   |               |   |       Product.java
|   |   |               |   |       
|   |   |               |   +---repository
|   |   |               |   |       ProductRepository.java
|   |   |               |   |       
|   |   |               |   \---service
|   |   |               |           ProductRepository.java
|   |   |               |           
|   |   |               +---transaction
|   |   |               |   +---controller
|   |   |               |   |       TransactionController.java
|   |   |               |   |       
|   |   |               |   +---model
|   |   |               |   |       TransactionAfterAuction.java
|   |   |               |   |       
|   |   |               |   +---repository
|   |   |               |   |       TransactionAfterAuctionRepository.java
|   |   |               |   |       
|   |   |               |   \---service
|   |   |               |           TransactionService.java
|   |   |               |           
|   |   |               \---user
|   |   |                   +---controller
|   |   |                   |       UserController.java
|   |   |                   |       
|   |   |                   +---model
|   |   |                   |       User.java
|   |   |                   |       
|   |   |                   +---repository
|   |   |                   |       UserRepository.java
|   |   |                   |       
|   |   |                   \---service
|   |   |                           UserService.java
|   |   |                           
|   |   \---resources
|   |           application.properties
|   |           
|   \---test
|       \---java
|           \---vn
|               \---team9
|                   \---auction_system
|                           AuctionSystemApplicationTests.java
|                           
\---target
    +---classes
    |   |   application.properties
    |   |   
    |   \---vn
    |       \---team9
    |           \---auction_system
    |               |   AuctionSystemApplication.class
    |               |   
    |               +---auction
    |               |   +---controller
    |               |   |       AuctionController.class
    |               |   |       
    |               |   +---model
    |               |   |       Auction.class
    |               |   |       Bid.class
    |               |   |       
    |               |   +---repository
    |               |   |       AuctionRepository.class
    |               |   |       BidRepository.class
    |               |   |       
    |               |   \---service
    |               |           AuctionService.class
    |               |           
    |               +---config
    |               |       SecurityConfig.class
    |               |       
    |               +---feedback
    |               |   +---controller
    |               |   |       AdminLogController.class
    |               |   |       FeedbackController.class
    |               |   |       NotificationController.class
    |               |   |       
    |               |   +---model
    |               |   |       AdminLog.class
    |               |   |       Feedback.class
    |               |   |       Notification.class
    |               |   |       
    |               |   +---repository
    |               |   |       AdminLogRepository.class
    |               |   |       FeedbackRepository.class
    |               |   |       NotificationRepository.class
    |               |   |       
    |               |   \---service
    |               |           AdminLogService.class
    |               |           FeedbackService.class
    |               |           NotificationService.class
    |               |           
    |               +---product
    |               |   +---controller
    |               |   |       ProductController.class
    |               |   |       
    |               |   +---model
    |               |   |       Image.class
    |               |   |       Product.class
    |               |   |       
    |               |   +---repository
    |               |   |       ProductRepository.class
    |               |   |       
    |               |   \---service
    |               |           ProductRepository.class
    |               |           
    |               +---transaction
    |               |   +---controller
    |               |   |       TransactionController.class
    |               |   |       
    |               |   +---model
    |               |   |       TransactionAfterAuction.class
    |               |   |       
    |               |   +---repository
    |               |   |       TransactionAfterAuctionRepository.class
    |               |   |       
    |               |   \---service
    |               |           TransactionService.class
    |               |           
    |               \---user
    |                   +---controller
    |                   |       UserController.class
    |                   |       
    |                   +---model
    |                   |       User.class
    |                   |       
    |                   +---repository
    |                   |       UserRepository.class
    |                   |       
    |                   \---service
    |                           UserService.class
    |                           
    +---generated-sources
    |   \---annotations
    +---generated-test-sources
    |   \---test-annotations
    \---test-classes
        \---vn
            \---team9
                \---auction_system
                        AuctionSystemApplicationTests.class
                        
