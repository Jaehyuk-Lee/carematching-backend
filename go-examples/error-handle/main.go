package main

import (
	"log"
	"os"
)

type error interface {
	Error() string
}

type MyError struct {
	Code int
	Msg  string
}

func (e MyError) Error() string {
	return e.Msg
}

func otherFunc() (int, error) {
	return 0, MyError{Code: 500, Msg: "custom error occurred"}
}

func main() {
	f, err := os.Open("C:\\temp\\1.txt")
	if err != nil {
		log.Fatal(err.Error())
	}
	println(f.Name())

	// 사용자 정의 에러를 잡을 때
	_, myErr := otherFunc()
	switch myErr.(type) {
	default: // no error
		println("ok")
	case MyError:
		log.Print("Log my error")
	case error:
		log.Fatal(myErr.Error())
	}
}
