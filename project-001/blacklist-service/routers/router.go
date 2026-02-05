package routers

import (
    "blacklist-service/controller"
    "blacklist-service/middleware"
    "github.com/gin-gonic/gin"
) 

func RegisterRoutes(router *gin.Engine, base64Secret string, blacklistedController *controller.BlackListedWalletController) {
    privateRoutes := router.Group("/blacklist")
    privateRoutes.Use(middleware.AuthenticationMiddleware(base64Secret))
    {
        privateRoutes.GET("/", blacklistedController.DefaultHome)
        privateRoutes.GET("/status/:walletID", blacklistedController.CheckWalletBlacklistStatus)
        privateRoutes.DELETE("/delete/:walletID", blacklistedController.RemoveBlacklistedWallet)
        privateRoutes.POST("/add", blacklistedController.AddToBlackList)
        privateRoutes.GET("/count", blacklistedController.CountBlacklistedWallets)
    }
}