package utils

import (
	"crypto/md5"
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"io"
	"math/big"
	"os"
	"time"
)

var idCounter uint32

func GenerateSessionID() string {
	const base58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
	result := make([]byte, 34)
	
	for i := 0; i < 34; i++ {
		n, _ := rand.Int(rand.Reader, big.NewInt(58))
		result[i] = base58[n.Int64()]
	}
	
	return string(result)
}

func GenerateERID() string {
	n, _ := rand.Int(rand.Reader, big.NewInt(90000))
	number := n.Int64() + 10000 
	return fmt.Sprintf("ER-%d", number)
}

func GenerateTerminalID() string {
	n, _ := rand.Int(rand.Reader, big.NewInt(9000))
	number := n.Int64() + 1000 
	return fmt.Sprintf("ATM-%d", number)
}

func GenerateReferenceNo() string {
	n, _ := rand.Int(rand.Reader, big.NewInt(9000000))
	number := n.Int64() + 1000000
	return fmt.Sprintf("REF%d", number)
}

func GenerateBitcoinWalletID() string {
	const base58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
	
	prefixes := []string{"1", "3"}
	prefixIndex, _ := rand.Int(rand.Reader, big.NewInt(int64(len(prefixes))))
	prefix := prefixes[prefixIndex.Int64()]

	length := 9

	address := make([]byte, length)
	for i := 0; i < length; i++ {
		n, _ := rand.Int(rand.Reader, big.NewInt(58))
		address[i] = base58[n.Int64()]
	}

	return prefix + string(address)
}

func randInt(min, max int) int {
	n, _ := rand.Int(rand.Reader, big.NewInt(int64(max-min+1)))
	return int(n.Int64()) + min
}

func GenerateUniqueRecipientId() string {
	data := make([]byte, 12)

	binaryTime := uint32(time.Now().Unix())
	data[0] = byte(binaryTime >> 24)
	data[1] = byte(binaryTime >> 16)
	data[2] = byte(binaryTime >> 8)
	data[3] = byte(binaryTime)

	pid := os.Getpid()
	data[4] = byte(pid >> 24)
	data[5] = byte(pid >> 16)
	data[6] = byte(pid >> 8)
	data[7] = byte(pid)

	idCounter++
	data[8] = byte(idCounter >> 24)
	data[9] = byte(idCounter >> 16)
	data[10] = byte(idCounter >> 8)
	data[11] = byte(idCounter)

	hash := md5.New()
	io.WriteString(hash, string(data))
	
	hashHex := hex.EncodeToString(hash.Sum(nil))[0:10]

	return "NX" + hashHex
}