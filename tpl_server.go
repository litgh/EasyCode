package main

import (
	"crypto/md5"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
)

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		b, _ := ioutil.ReadAll(r.Body)
		h := md5.New()
		h.Write(b)
		md5Sum := fmt.Sprintf("%x", h.Sum(nil))
		fmt.Println(md5Sum, string(b))
		ioutil.WriteFile(md5Sum, b, 0444)
		rs := make(map[string]interface{})
		rs["code"] = 0
		rs["data"] = "token:" + md5Sum
		j, _ := json.Marshal(rs)
		fmt.Println(string(j))
		w.Write(j)
	})
	http.HandleFunc("/template", func(w http.ResponseWriter, r *http.Request) {
		token := r.URL.Query().Get("token")
		fmt.Println(token)
		b, err := ioutil.ReadFile(token)
		if err != nil {
			d, _ := json.Marshal(struct {
				Code int    `json:"code"`
				Msg  string `json:"msg"`
			}{
				1,
				err.Error(),
			})
			w.Write(d)
			return
		}
		d, _ := json.Marshal(struct {
			Code int    `json:"code"`
			Data string `json:"data"`
		}{
			0,
			string(b),
		})
		w.Write(d)
	})
	http.ListenAndServe(":8899", nil)
}
