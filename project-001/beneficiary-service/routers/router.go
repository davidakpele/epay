package routers

import (
	"beneficiary-service/controller"
	"beneficiary-service/middleware"
	"github.com/gin-gonic/gin"
)

// RegisterRoutes initializes the routes for the application
func RegisterRoutes(router *gin.Engine, base64Secret string, beneficiaryController *controller.BeneficiaryController) {
	privateRoutes := router.Group("/beneficiary")
	privateRoutes.Use(middleware.AuthenticationMiddleware(base64Secret))
	{
		// Create beneficiary (bank or user)
		privateRoutes.POST("/create", beneficiaryController.CreateBeneficiary)

		// Get single beneficiary by user ID (backward compatibility)
		privateRoutes.GET("/:userID", beneficiaryController.GetBeneficiaryByUserID)

		// Get all beneficiaries for a user
		privateRoutes.GET("/all/:userID", beneficiaryController.GetAllBeneficiariesByUserID)

		// Get beneficiaries filtered by type (bank or user)
		// Query param: ?type=bank or ?type=user
		privateRoutes.GET("/type/:userID", beneficiaryController.GetBeneficiariesByType)

		// Search beneficiaries by name, account number, or username
		// Query param: ?search=term
		privateRoutes.GET("/search/:userID", beneficiaryController.SearchBeneficiaries)

		// Update beneficiary
		privateRoutes.PUT("/update/:id", beneficiaryController.UpdateBeneficiary)

		// Delete single beneficiary
		// Query param: ?userId=123
		privateRoutes.DELETE("/delete/:id", beneficiaryController.DeleteBeneficiary)

		// Delete multiple beneficiaries by IDs
		privateRoutes.DELETE("/delete", beneficiaryController.DeleteBeneficiariesByIDs)
	}
}