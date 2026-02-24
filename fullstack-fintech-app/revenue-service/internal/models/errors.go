package models

import "fmt"

type InvalidCurrencyError struct {
    Currency string
}

func (e *InvalidCurrencyError) Error() string {
    return fmt.Sprintf("unknown currency type: %s", e.Currency)
}