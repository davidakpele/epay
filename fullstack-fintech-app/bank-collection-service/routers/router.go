package routers

import (
    "bank-collection-service/controller"
    "bank-collection-service/middleware"
    "github.com/gin-gonic/gin"
)

func RegisterRoutes(router *gin.Engine, base64Secret string, bankController *controller.BankController, virtualCardController *controller.CardController) {
    privateRoutes := router.Group("/bank")
    privateRoutes.Use(middleware.AuthenticationMiddleware(base64Secret))
    {
        privateRoutes.POST("/create/virtual-card", virtualCardController.CreateVirtualCard)
        privateRoutes.GET("/virtual-card/:cardId", virtualCardController.GetVirtualCardByID)
        privateRoutes.GET("/:userId/cards", virtualCardController.GetAllUserVirtualCards)
        privateRoutes.POST("/create", bankController.CreateBankList)
        privateRoutes.GET("/details/:id", bankController.GetBankById)
        privateRoutes.GET("/accounts/:accountNumber", bankController.GetBankByAccountNumber)
        privateRoutes.GET("/users/:id", bankController.GetBanksByUserId)
        privateRoutes.DELETE("/delete/:id", bankController.DeleteBanksByIds)
        privateRoutes.GET("/bank-list", bankController.FetchAllBanks)
        privateRoutes.GET("/verify-user-bank-details", bankController.VerifyBankAccountExternal)
        privateRoutes.GET("/user/bank", bankController.VerifyBankAccountInternally)
    }
}