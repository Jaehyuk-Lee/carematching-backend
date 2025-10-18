package main

import (
	"fmt"
)

type A struct {
	name string
	age  int
}

func (a A) String() string {
	return a.name + " (" + fmt.Sprint(a.age) + ")"
}

type AI interface {
	String() string
}

var condition = false

func billo() AI {
	var a *A = nil
	if condition {
		a = &A{}
		a.name = "billo"
		a.age = 30
	}
	return a
}

func main() {
	fmt.Println(billo())
	fmt.Println(billo() == nil)
}
