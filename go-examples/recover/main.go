package main

import (
	"fmt"
	"os"
)

func main() {
	// 잘못된 파일명을 넣음
	openFile("Invalid.txt")

	// recover에 의해
	// 이 문장 실행됨  println("Done")
}

func openFile(fn string) {
	// defer 함수. panic 호출시 실행됨
	defer func() {
		if r := recover(); r != nil {
			fmt.Println("OPEN ERROR", r)
		}
	}()

	f, err := os.Open(fn)
	if err != nil {
		panic(err)
	}

	defer f.Close()
}
