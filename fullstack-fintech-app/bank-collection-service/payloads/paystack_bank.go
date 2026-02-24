package payloads

type PayStackBankList struct {
    ID               int    `json:"id"`
    Name             string `json:"name"`
    Slug             string `json:"slug"`
    Code             string `json:"code"`
    Longcode         string `json:"longcode"`
    Gateway          string `json:"gateway"`
    PayWithBank      bool   `json:"pay_with_bank"`
    SupportsTransfer bool   `json:"supports_transfer"`
    Active           bool   `json:"active"`
    Country          string `json:"country"`
    Currency         string `json:"currency"`
    Type             string `json:"type"`
    IsDeleted        bool   `json:"is_deleted"`
    CreatedAt        string `json:"createdAt"`
    UpdatedAt        string `json:"updatedAt"`
}

type PaystackBankResponse struct {
    Status  bool               `json:"status"`
    Message string             `json:"message"`
    Data    []PayStackBankList `json:"data"`
}