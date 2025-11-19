Folder PATH listing for volume New Volume
Volume serial number is 66F7-B452
F:.
ª   BACKEND_STRUCTURE.md
ª   mvnw
ª   mvnw.cmd
ª   pom.xml
ª   structure.txt
ª   
+---.idea
ª       .gitignore
ª       compiler.xml
ª       encodings.xml
ª       jarRepositories.xml
ª       misc.xml
ª       vcs.xml
ª       workspace.xml
ª       
+---.mvn
ª   +---wrapper
ª           maven-wrapper.properties
ª           
+---src
    +---main
    ª   +---java
    ª   ª   +---vn
    ª   ª       +---team9
    ª   ª           +---auction_system
    ª   ª               ª   AuctionSystemApplication.java
    ª   ª               ª   
    ª   ª               +---auction
    ª   ª               ª   +---controller
    ª   ª               ª   ª       AuctionController.java
    ª   ª               ª   ª       
    ª   ª               ª   +---mapper
    ª   ª               ª   ª       AuctionMapper.java
    ª   ª               ª   ª       
    ª   ª               ª   +---model
    ª   ª               ª   ª       Auction.java
    ª   ª               ª   ª       Bid.java
    ª   ª               ª   ª       
    ª   ª               ª   +---repository
    ª   ª               ª   ª       AuctionRepository.java
    ª   ª               ª   ª       BidRepository.java
    ª   ª               ª   ª       
    ª   ª               ª   +---service
    ª   ª               ª           AuctionService.java
    ª   ª               ª           
    ª   ª               +---common
    ª   ª               ª   +---base
    ª   ª               ª   ª       BaseRequest.java
    ª   ª               ª   ª       BaseResponse.java
    ª   ª               ª   ª       BaseService.java
    ª   ª               ª   ª       
    ª   ª               ª   +---dto
    ª   ª               ª   ª   +---account
    ª   ª               ª   ª   ª       AccountTransactionRequest.java
    ª   ª               ª   ª   ª       AccountTransactionResponse.java
    ª   ª               ª   ª   ª       
    ª   ª               ª   ª   +---admin
    ª   ª               ª   ª   ª       AdminLogRequest.java
    ª   ª               ª   ª   ª       AdminLogResponse.java
    ª   ª               ª   ª   ª       
    ª   ª               ª   ª   +---auction
    ª   ª               ª   ª   ª       AuctionRequest.java
    ª   ª               ª   ª   ª       AuctionResponse.java
    ª   ª               ª   ª   ª       BidRequest.java
    ª   ª               ª   ª   ª       BidResponse.java
    ª   ª               ª   ª   ª       
    ª   ª               ª   ª   +---feedback
    ª   ª               ª   ª   ª       FeedbackRequest.java
    ª   ª               ª   ª   ª       FeedbackResponse.java
    ª   ª               ª   ª   ª       
    ª   ª               ª   ª   +---image
    ª   ª               ª   ª   ª       ImageRequest.java
    ª   ª               ª   ª   ª       ImageResponse.java
    ª   ª               ª   ª   ª       
    ª   ª               ª   ª   +---notification
    ª   ª               ª   ª   ª       NotificationRequest.java
    ª   ª               ª   ª   ª       NotificationResponse.java
    ª   ª               ª   ª   ª       
    ª   ª               ª   ª   +---product
    ª   ª               ª   ª   ª       ProductRequest.java
    ª   ª               ª   ª   ª       ProductResponse.java
    ª   ª               ª   ª   ª       
    ª   ª               ª   ª   +---transaction
    ª   ª               ª   ª   ª       TransactionAfterAuctionRequest.java
    ª   ª               ª   ª   ª       TransactionAfterAuctionResponse.java
    ª   ª               ª   ª   ª       
    ª   ª               ª   ª   +---user
    ª   ª               ª   ª           UserRequest.java
    ª   ª               ª   ª           UserResponse.java
    ª   ª               ª   ª           
    ª   ª               ª   +---service
    ª   ª               ª           IAccountTransactionService.java
    ª   ª               ª           IAdminLogService.java
    ª   ª               ª           IAuctionService.java
    ª   ª               ª           IBidService.java
    ª   ª               ª           IFeedbackService.java
    ª   ª               ª           INotificationService.java
    ª   ª               ª           IProductService.java
    ª   ª               ª               sactionAfterAuctionService.java
    ª   ª               ª           IUserService.java
    ª   ª               ª           
    ª   ª               +---config
    ª   ª               ª       SecurityConfig.java
    ª   ª               ª       
    ª   ª               +---feedback
    ª   ª               ª   +---controller
    ª   ª               ª   ª       AdminLogController.java
    ª   ª               ª   ª       FeedbackController.java
    ª   ª               ª   ª       NotificationController.java
    ª   ª               ª   ª       
    ª   ª               ª   +---mapper
    ª   ª               ª   ª       FeedbackMapper.java
    ª   ª               ª   ª       
    ª   ª               ª   +---model
    ª   ª               ª   ª       AdminLog.java
    ª   ª               ª   ª       Feedback.java
    ª   ª               ª   ª       Notification.java
    ª   ª               ª   ª       
    ª   ª               ª   +---repository
    ª   ª               ª   ª       AdminLogRepository.java
    ª   ª               ª   ª       FeedbackRepository.java
    ª   ª               ª   ª       NotificationRepository.java
    ª   ª               ª   ª       
    ª   ª               ª   +---service
    ª   ª               ª           AdminLogService.java
    ª   ª               ª           FeedbackService.java
    ª   ª               ª           NotificationService.java
    ª   ª               ª           
    ª   ª               +---product
    ª   ª               ª   +---controller
    ª   ª               ª   ª       ProductController.java
    ª   ª               ª   ª       
    ª   ª               ª   +---mapper
    ª   ª               ª   ª       ProductMapper.java
    ª   ª               ª   ª       
    ª   ª               ª   +---model
    ª   ª               ª   ª       Category.java
    ª   ª               ª   ª       Image.java
    ª   ª               ª   ª       Product.java
    ª   ª               ª   ª       
    ª   ª               ª   +---repository
    ª   ª               ª   ª       ProductRepository.java
    ª   ª               ª   ª       
    ª   ª               ª   +---service
    ª   ª               ª           ProductService.java
    ª   ª               ª           
    ª   ª               +---transaction
    ª   ª               ª   +---controller
    ª   ª               ª   ª       TransactionController.java
    ª   ª               ª   ª       
    ª   ª               ª   +---mapper
    ª   ª               ª   ª       TransactionMapper.java
    ª   ª               ª   ª       
    ª   ª               ª   +---model
    ª   ª               ª   ª       AccountTransaction.java
    ª   ª               ª   ª       TransactionAfterAuction.java
    ª   ª               ª   ª       
    ª   ª               ª   +---repository
    ª   ª               ª   ª       TransactionAfterAuctionRepository.java
    ª   ª               ª   ª       
    ª   ª               ª   +---service
    ª   ª               ª           TransactionService.java
    ª   ª               ª           
    ª   ª               +---user
    ª   ª                   +---controller
    ª   ª                   ª       UserController.java
    ª   ª                   ª       
    ª   ª                   +---mapper
    ª   ª                   ª       UserMapper.java
    ª   ª                   ª       
    ª   ª                   +---model
    ª   ª                   ª       Role.java
    ª   ª                   ª       User.java
    ª   ª                   ª       
    ª   ª                   +---repository
    ª   ª                   ª       UserRepository.java
    ª   ª                   ª       
    ª   ª                   +---service
    ª   ª                           UserService.java
    ª   ª                           
    ª   +---resources
    ª           application.properties
    ª           
    +---test
        +---java
            +---vn
                +---team9
                    +---auction_system
                            AuctionSystemApplicationTests.java
                            
