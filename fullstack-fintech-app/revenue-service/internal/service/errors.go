package service

type InsufficientBalanceError struct{}

func (e *InsufficientBalanceError) Error() string {
    return "insufficient balance"
}

type PasswordRequiredError struct{}

func (e *PasswordRequiredError) Error() string {
    return "password is required for withdrawal"
}

type InvalidPasswordError struct{}

func (e *InvalidPasswordError) Error() string {
    return "invalid password"
}