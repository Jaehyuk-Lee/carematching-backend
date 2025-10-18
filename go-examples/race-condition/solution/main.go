// 상태는 오직 owner 고루틴만 읽고/쓴다.
package main

import (
	"fmt"
	"runtime"
)

type incReq struct {
	delta int
	reply chan int // 현재 값 응답
}

func main() {
	runtime.GOMAXPROCS(2) // 병렬 환경 가정

	ops := make(chan incReq) // 요청 채널
	done := make(chan struct{})

	// ② 상태 소유자(단일 고루틴)
	go func() {
		counter := 0
		for {
			select {
			case req := <-ops: // ③ 요청을 FIFO로 하나씩 처리
				counter += req.delta
				req.reply <- counter // ④ 응답
			case <-done:
				return
			}
		}
	}()

	// ① 동시에 실행되는 작업들(여러 고루틴)
	replyA := make(chan int)
	replyB := make(chan int)

	go func() { // 고루틴 A
		for i := 0; i < 100000; i++ {
			ops <- incReq{delta: 1, reply: replyA} // ⑤ 송신 시점 동기화
			<-replyA                               // ⑥ 처리 완료까지 대기(선택)
		}
	}()

	go func() { // 고루틴 B
		for i := 0; i < 100000; i++ {
			ops <- incReq{delta: 1, reply: replyB}
			<-replyB
		}
	}()

	// 모두 끝났다고 가정하는 간단한 동기화(데모용)
	for i := 0; i < 200000; i++ {
		<-replyA        // A가 받은 응답 일부
		if i == 99999 { // A가 10만 번 받으면 이후는 B 응답으로 전환
			break
		}
	}
	for i := 0; i < 100000; i++ {
		<-replyB
	}

	close(done)
	fmt.Println("OK")
}
