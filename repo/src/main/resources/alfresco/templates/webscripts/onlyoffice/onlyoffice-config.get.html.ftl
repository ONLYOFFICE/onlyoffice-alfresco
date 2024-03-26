<#include "/org/alfresco/repository/admin/admin-template.ftl" />

<!--
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
-->

<@page title=msg("onlyoffice-config.title") readonly=true>

<head>
    <style type="text/css">
        #onlyoffice-cloud-banner {
            background: #f5f5f5;
            box-sizing: border-box;
            margin-top: 50px;
            margin-bottom: 50px;
            width: 570px;
        }

        #onlyoffice-cloud-banner .onlyoffice-cloud-banner-info {
            background: url("data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzUzIiBoZWlnaHQ9IjExNiIgdmlld0JveD0iMCAwIDM1MyAxMTYiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxtYXNrIGlkPSJtYXNrMF8yNzgzXzE0MDI4NSIgc3R5bGU9Im1hc2stdHlwZTphbHBoYSIgbWFza1VuaXRzPSJ1c2VyU3BhY2VPblVzZSIgeD0iMCIgeT0iMCIgd2lkdGg9IjM1MyIgaGVpZ2h0PSIxMTYiPgo8cmVjdCB3aWR0aD0iMzUzIiBoZWlnaHQ9IjExNiIgcng9IjMiIGZpbGw9IiNBNkRDRjIiLz4KPC9tYXNrPgo8ZyBtYXNrPSJ1cmwoI21hc2swXzI3ODNfMTQwMjg1KSI+CjxwYXRoIG9wYWNpdHk9IjAuMiIgZmlsbC1ydWxlPSJldmVub2RkIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik0yMDAuMzEyIDQyLjAxMThMMzE5LjQ2OSA1LjQ0MDE5QzMyOS44MDkgMi4yMDkzOSAzMzAuOTEzIC01LjExMDEzIDMyMS45MzcgLTEwLjkyMTFMMjE3LjEyIC04Mi44NDMxQzIwNi40MjggLTkwLjg1MjUgMTk2LjI2IC05My44MzY3IDE4MC42MDMgLTg4LjAzNjlMNjEuNjE5NSAtNTEuNjMxQzUxLjI3OTIgLTQ4LjQwMDIgNTAuMTc1MiAtNDEuMDgwNyA1OS4xNTE2IC0zNS4yNjk3TDE1OS4yMjkgMzYuNzc0NUMxNjkuOTgyIDQ1LjE4NDkgMTgwLjc1OCA0Ny4zMDk5IDIwMC4zMTIgNDIuMDExOFoiIGZpbGw9IiNEOUY0RkYiLz4KPHBhdGggb3BhY2l0eT0iMC4yNSIgZmlsbC1ydWxlPSJldmVub2RkIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik02MS42NTU5IDc4Ljg1OTRMLTYxLjc0ODUgNTYuMTQ3MkMtNzIuMjkgNTQuMTAxMSAtNzQuMTU3NyA0Ny4wMDM3IC02NS45MjMzIDQwLjI4MjNMMjkuNzk1MiAtNDIuMjE0NEMzNy45MTk3IC00OS4zNTMyIDUyLjkzMzcgLTUzLjQ0OTUgNjMuMDY5NCAtNTEuMjkyOEwxODYuNDc0IC0yOC41ODA1QzE5Ny4wMTUgLTI2LjUzNDUgMTk4Ljg4MyAtMTkuNDM3IDE5MC42NDkgLTEyLjcxNTdMOTQuOTMwMSA2OS43ODFDODUuNDg0MyA3OC4xNTE5IDcxLjY4MTcgODAuNTk4NyA2MS42NTU5IDc4Ljg1OTRaIiBmaWxsPSIjRDlGNEZGIi8+CjxwYXRoIG9wYWNpdHk9IjAuMyIgZmlsbC1ydWxlPSJldmVub2RkIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik05MC45NTMxIDI0LjAwOTdMMTAxLjM0NyAtMTAzLjI0MkMxMDIuMTM0IC0xMTQuMTM5IDk1Ljg4NDkgLTExNy44MTMgODcuMzc3NiAtMTExLjQ1NEwtMTUuNjU0NyAtMzguNjYyN0MtMjQuNTI5NiAtMzIuNTE5NSAtMzIuMzAxNCAtMTguODEyOSAtMzIuODc4NyAtOC4yODU3OUwtNDMuMjcyNiAxMTguOTY2Qy00NC4wNiAxMjkuODYzIC0zNy44MTA2IDEzMy41MzcgLTI5LjMwMzMgMTI3LjE3OEw3My43MjkxIDU0LjM4NjZDODQuMTE0NyA0Ny4yNjI4IDkwLjAwODEgMzQuMzIwNyA5MC45NTMxIDI0LjAwOTdaIiBmaWxsPSIjRDlGNEZGIi8+CjwvZz4KPC9zdmc+Cg==") no-repeat;
            box-sizing: border-box;
            display: inline-block;
            padding: 15px 0px 15px 15px;
            position: relative;
            width: 70%;
        }

        #onlyoffice-cloud-banner img {
            bottom: 0;
            display: inline-block;
            margin: auto;
            position: absolute;
            top: 0;
            content:url("data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODIiIGhlaWdodD0iNzkiIHZpZXdCb3g9IjAgMCA4MiA3OSIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTYzLjYxNzYgNjZIMTQuMzgyNEMxMy4wMzI0IDY2IDEyIDY0Ljk2IDEyIDYzLjZWMjguNEMxMiAyNy4wNCAxMy4wMzI0IDI2IDE0LjM4MjQgMjZINjMuNjE3NkM2NC45Njc2IDI2IDY2IDI3LjA0IDY2IDI4LjRWNjMuNkM2NiA2NC45NiA2NC45Njc2IDY2IDYzLjYxNzYgNjZaIiBmaWxsPSIjRUZFRkVGIi8+CjxwYXRoIGQ9Ik0xMi4wMDAxIDI4LjE4MDVDMTIuMDAwMSAyNy4yOTQzIDEzLjI4NzIgMjYgMTQuMTcwNiAyNkg2Mi44MjkzQzYzLjYzMjEgMjYgNjQuNzY4NSAyNiA2NC45NjkyIDI3LjUwMDlDNjUuMDA1OCAyNy43NzQ2IDY1LjIyNCAyOCA2NS41MDAxIDI4QzY1Ljc3NjIgMjggNjYuMDA0NiAyNy43NzQ3IDY1Ljk2MjggMjcuNTAxN0M2NS43MzA4IDI1Ljk4ODggNjQuNDQzMiAyNSA2Mi44MjkzIDI1SDE0LjE3MDZDMTIuMzg1NyAyNSAxMSAyNi4zOSAxMSAyOC4xODA1VjQyLjk5OTlDMTEgNDMuNTUyMiAxMS40NDc3IDQ0IDEyLjAwMDEgNDRWMjguMTgwNVoiIGZpbGw9IiM0NDQ0NDQiLz4KPHBhdGggZD0iTTYzLjYxNzYgNjZIMTQuMzgyNEMxMy4wMzI0IDY2IDEyIDY0Ljk3MjEgMTIgNjMuNjI3OVYzMkg2NlY2My42Mjc5QzY2IDY0Ljk3MjEgNjQuOTY3NiA2NiA2My42MTc2IDY2WiIgZmlsbD0id2hpdGUiLz4KPHBhdGggZmlsbC1ydWxlPSJldmVub2RkIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik0xMSAzMi44MzA1QzExIDMyLjM3MTggMTEuMzY1NCAzMiAxMS44MTYyIDMySDUwLjVDNTAuNzc2MSAzMiA1MSAzMi4yMjM5IDUxIDMyLjVDNTEgMzIuNzc2MSA1MC43NzYxIDMzIDUwLjUgMzNIMTJWNDUuNTM1NUMxMiA0NS44MTE2IDExLjc3NjEgNDYuMDM1NSAxMS41IDQ2LjAzNTVDMTEuMjIzOSA0Ni4wMzU1IDExIDQ1LjgxMTYgMTEgNDUuNTM1NVYzMi44MzA1Wk02NS41IDU4QzY1LjIyMzkgNTggNjUgNTguMjIzOSA2NSA1OC41VjU5LjVDNjUgNTkuNzc2MSA2NS4yMjM5IDYwIDY1LjUgNjBDNjUuNzc2MSA2MCA2NiA1OS43NzYxIDY2IDU5LjVWNTguNUM2NiA1OC4yMjM5IDY1Ljc3NjEgNTggNjUuNSA1OFpNMzggNjcuNUMzOCA2Ny4yMjM5IDM4LjIyMzkgNjcgMzguNSA2N0g0MC41QzQwLjc3NjEgNjcgNDEgNjcuMjIzOSA0MSA2Ny41QzQxIDY3Ljc3NjEgNDAuNzc2MSA2OCA0MC41IDY4SDM4LjVDMzguMjIzOSA2OCAzOCA2Ny43NzYxIDM4IDY3LjVaIiBmaWxsPSIjNDQ0NDQ0Ii8+CjxwYXRoIGQ9Ik0xNSAzMEMxNS41NTIzIDMwIDE2IDI5LjU1MjMgMTYgMjlDMTYgMjguNDQ3NyAxNS41NTIzIDI4IDE1IDI4QzE0LjQ0NzcgMjggMTQgMjguNDQ3NyAxNCAyOUMxNCAyOS41NTIzIDE0LjQ0NzcgMzAgMTUgMzBaIiBmaWxsPSIjNDQ0NDQ0Ii8+CjxwYXRoIGQ9Ik0xOSAzMEMxOS41NTIzIDMwIDIwIDI5LjU1MjMgMjAgMjlDMjAgMjguNDQ3NyAxOS41NTIzIDI4IDE5IDI4QzE4LjQ0NzcgMjggMTggMjguNDQ3NyAxOCAyOUMxOCAyOS41NTIzIDE4LjQ0NzcgMzAgMTkgMzBaIiBmaWxsPSIjNDQ0NDQ0Ii8+CjxwYXRoIGQ9Ik0yMyAzMEMyMy41NTIzIDMwIDI0IDI5LjU1MjMgMjQgMjlDMjQgMjguNDQ3NyAyMy41NTIzIDI4IDIzIDI4QzIyLjQ0NzcgMjggMjIgMjguNDQ3NyAyMiAyOUMyMiAyOS41NTIzIDIyLjQ0NzcgMzAgMjMgMzBaIiBmaWxsPSIjNDQ0NDQ0Ii8+CjxyZWN0IHg9IjE1IiB5PSIzNSIgd2lkdGg9IjMwIiBoZWlnaHQ9IjEiIHJ4PSIwLjUiIGZpbGw9IiNFRkVGRUYiLz4KPHJlY3QgeD0iMTciIHk9IjM4IiB3aWR0aD0iMzAiIGhlaWdodD0iMSIgcng9IjAuNSIgZmlsbD0iI0VGRUZFRiIvPgo8cmVjdCB4PSIxNSIgeT0iNDEiIHdpZHRoPSIzMCIgaGVpZ2h0PSIxIiByeD0iMC41IiBmaWxsPSIjRUZFRkVGIi8+CjxwYXRoIGQ9Ik0zMCAyNUwzOCAxN0w0NiAyNSIgc3Ryb2tlPSIjNDQ0NDQ0IiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiLz4KPHBhdGggZD0iTTQwLjE0NjUgMTMuNDI3OFYxMC44NDA4SDM4Ljk5OThIMzcuODUzVjE0LjA3NDZDMzcuODUzIDE0LjA3NDYgMzkuOTkzNiAxNC42NDA1IDM5Ljk5MzYgMTYuNzQyNUMzOS45OTM2IDE4LjI3ODYgMzguODQ2OSAxOS40OTEyIDM3LjM5NDMgMTkuNDkxMkMzNi4wOTQ2IDE5LjQ5MTIgMzUuMDI0MiAxOC41MjExIDM0Ljg3MTMgMTcuMjI3NkMzNC44NzEzIDE3LjM4OTMgMzQuNzk0OSAxNy42MzE4IDM0Ljc5NDkgMTcuNzkzNUMzNC43OTQ5IDIwLjIxODkgMzYuNzA2MiAyMi4xNTkyIDM4Ljk5OTggMjIuMTU5MkM0MS4yOTMzIDIyLjE1OTIgNDMuMjA0NyAyMC4yMTg5IDQzLjIwNDcgMTcuNzkzNUM0My4yMDQ3IDE1LjYxMDcgNDEuOTgxNCAxMy45MTI5IDQwLjE0NjUgMTMuNDI3OFoiIGZpbGw9IiNFRkVGRUYiLz4KPHBhdGggZD0iTTQwLjQ0MTkgMTIuODE0OUM0MC40NDE5IDEzLjAxNzMgNDAuNTYzOSAxMy4xOTk4IDQwLjc1MSAxMy4yNzdDNDIuNDA5NCAxMy45NjE5IDQzLjUgMTUuNjY1NiA0My41IDE3Ljc5MzVDNDMuNSAyMC40NDEgNDEuNDIzNyAyMi41IDM5IDIyLjVDMzYuNTc2MyAyMi41IDM0LjUgMjAuNDQxIDM0LjUgMTcuNzkzNUMzNC41IDE3LjcyNTIgMzQuNTA3OSAxNy42NTgxIDM0LjUxNjIgMTcuNjAyM0MzNC41MjQzIDE3LjU0ODEgMzQuNTM0OCAxNy40OTIxIDM0LjU0MzggMTcuNDQ0NUwzNC41NDQ5IDE3LjQzODZDMzQuNTU0NyAxNy4zODcgMzQuNTYyOSAxNy4zNDM3IDM0LjU2ODcgMTcuMzA0MkMzNC41NzA3IDE3LjI5MTEgMzQuNTcyMSAxNy4yODAyIDM0LjU3MzIgMTcuMjcxNEMzNC41NzUxIDE3LjI1NDcgMzQuNTc2MiAxNy4yMzggMzQuNTc2NSAxNy4yMjEyQzM0LjU3OTcgMTcuMDE5OSAzNC43MjM0IDE2Ljg5NiAzNC44NTUgMTYuODg3NEMzNC45ODUxIDE2Ljg3ODggMzUuMTQwMyAxNi45ODE1IDM1LjE2NCAxNy4xODJDMzUuMjkyOSAxOC4yNzI1IDM2LjIwOTEgMTkuMTUwNSAzNy4zOTQ1IDE5LjE1MDVDMzguNzEwMSAxOS4xNTA1IDM5LjY5ODggMTguMDYzNyAzOS42OTg4IDE2Ljc0MjVDMzkuNjk4OCAxNS44NDk3IDM5LjI1MiAxNS4yNzUxIDM4Ljc2MjggMTQuOTA1NUMzOC41MTg5IDE0LjcyMTMgMzguMjcyNiAxNC41OTU0IDM4LjA4NjEgMTQuNTE1NkMzNy45OTMzIDE0LjQ3NTkgMzcuOTE2NiAxNC40NDgxIDM3Ljg2NCAxNC40MzA1QzM3LjgzNzcgMTQuNDIxNyAzNy44MTc2IDE0LjQxNTUgMzcuODA0NiAxNC40MTE3TDM3LjgwMTkgMTQuNDEwOUwzNy43OTU2IDE0LjQwOUwzNy43ODY2IDE0LjQwNjVDMzcuNjY4OSAxNC4zNzQ2IDM3LjU1ODEgMTQuMjQ5OSAzNy41NTgxIDE0LjA3NDZWMTAuODQwOEMzNy41NTgxIDEwLjYyNTkgMzcuNzE2MiAxMC41IDM3Ljg1MzIgMTAuNUg0MC4xNDY4QzQwLjI4MzggMTAuNSA0MC40NDE5IDEwLjYyNTkgNDAuNDQxOSAxMC44NDA4VjEyLjgxNDlaIiBmaWxsPSIjQUFBQUFBIiBzdHJva2U9IiM0NDQ0NDQiIHN0cm9rZS1taXRlcmxpbWl0PSIxMCIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIi8+CjxwYXRoIGQ9Ik04MCAzMEg1NVY1NUg4MFYzMFoiIGZpbGw9IndoaXRlIi8+CjxwYXRoIGZpbGwtcnVsZT0iZXZlbm9kZCIgY2xpcC1ydWxlPSJldmVub2RkIiBkPSJNNTQuNSAzMkM1NC4yMjM5IDMyIDU0IDMxLjc3NjEgNTQgMzEuNVYzMEM1NCAyOS40NDc3IDU0LjQ0NzcgMjkgNTUgMjlINTYuNUM1Ni43NzYxIDI5IDU3IDI5LjIyMzkgNTcgMjkuNUM1NyAyOS43NzYxIDU2Ljc3NjEgMzAgNTYuNSAzMEg1NVYzMS41QzU1IDMxLjc3NjEgNTQuNzc2MSAzMiA1NC41IDMyWk02NS4yNSAyOS41QzY1LjI1IDI5LjIyMzkgNjUuMDI2MSAyOSA2NC43NSAyOUg2MS4yNUM2MC45NzM5IDI5IDYwLjc1IDI5LjIyMzkgNjAuNzUgMjkuNUM2MC43NSAyOS43NzYxIDYwLjk3MzkgMzAgNjEuMjUgMzBINjQuNzVDNjUuMDI2MSAzMCA2NS4yNSAyOS43NzYxIDY1LjI1IDI5LjVaTTcwLjI1IDI5QzY5Ljk3MzkgMjkgNjkuNzUgMjkuMjIzOSA2OS43NSAyOS41QzY5Ljc1IDI5Ljc3NjEgNjkuOTczOSAzMCA3MC4yNSAzMEg3My43NUM3NC4wMjYxIDMwIDc0LjI1IDI5Ljc3NjEgNzQuMjUgMjkuNUM3NC4yNSAyOS4yMjM5IDc0LjAyNjEgMjkgNzMuNzUgMjlINzAuMjVaTTc4LjUgMjlDNzguMjI0IDI5IDc4IDI5LjIyMzkgNzggMjkuNUM3OCAyOS43NzYxIDc4LjIyNCAzMCA3OC41IDMwSDgwVjMxLjVDODAgMzEuNzc2MSA4MC4yMjQgMzIgODAuNSAzMkM4MC43NzYgMzIgODEgMzEuNzc2MSA4MSAzMS41VjMwQzgxIDI5LjQ0NzcgODAuNTUyIDI5IDgwIDI5SDc5LjVINzguNVpNODEgMzYuMjVDODEgMzUuOTczOSA4MC43NzYgMzUuNzUgODAuNSAzNS43NUM4MC4yMjQgMzUuNzUgODAgMzUuOTczOSA4MCAzNi4yNVYzOS43NUM4MCA0MC4wMjYxIDgwLjIyNCA0MC4yNSA4MC41IDQwLjI1QzgwLjc3NiA0MC4yNSA4MSA0MC4wMjYxIDgxIDM5Ljc1VjM2LjI1Wk04MSA0NS4yNUM4MSA0NC45NzM5IDgwLjc3NiA0NC43NSA4MC41IDQ0Ljc1QzgwLjIyNCA0NC43NSA4MCA0NC45NzM5IDgwIDQ1LjI1VjQ4Ljc1QzgwIDQ5LjAyNjEgODAuMjI0IDQ5LjI1IDgwLjUgNDkuMjVDODAuNzc2IDQ5LjI1IDgxIDQ5LjAyNjEgODEgNDguNzVWNDUuMjVaTTgxIDUzLjVDODEgNTMuMjIzOSA4MC43NzYgNTMgODAuNSA1M0M4MC4yMjQgNTMgODAgNTMuMjIzOSA4MCA1My41VjU1SDc4LjVDNzguMjI0IDU1IDc4IDU1LjIyMzkgNzggNTUuNUM3OCA1NS43NzYxIDc4LjIyNCA1NiA3OC41IDU2SDgwQzgwLjU1MiA1NiA4MSA1NS41NTIzIDgxIDU1VjUzLjVaTTczLjc1IDU2Qzc0LjAyNjEgNTYgNzQuMjUgNTUuNzc2MSA3NC4yNSA1NS41Qzc0LjI1IDU1LjIyMzkgNzQuMDI2MSA1NSA3My43NSA1NUg3MC4yNUM2OS45NzM5IDU1IDY5Ljc1IDU1LjIyMzkgNjkuNzUgNTUuNUM2OS43NSA1NS43NzYxIDY5Ljk3MzkgNTYgNzAuMjUgNTZINzMuNzVaTTY0Ljc1IDU2QzY1LjAyNjEgNTYgNjUuMjUgNTUuNzc2MSA2NS4yNSA1NS41QzY1LjI1IDU1LjIyMzkgNjUuMDI2MSA1NSA2NC43NSA1NUg2MS4yNUM2MC45NzM5IDU1IDYwLjc1IDU1LjIyMzkgNjAuNzUgNTUuNUM2MC43NSA1NS43NzYxIDYwLjk3MzkgNTYgNjEuMjUgNTZINjQuNzVaTTU2LjUgNTZDNTYuNzc2MSA1NiA1NyA1NS43NzYxIDU3IDU1LjVDNTcgNTUuMjIzOSA1Ni43NzYxIDU1IDU2LjUgNTVINTVWNTMuNUM1NSA1My4yMjM5IDU0Ljc3NjEgNTMgNTQuNSA1M0M1NC4yMjM5IDUzIDU0IDUzLjIyMzkgNTQgNTMuNVY1NUM1NCA1NS41NTIzIDU0LjQ0NzcgNTYgNTUgNTZINTYuNVpNNTQgNDguNzVDNTQgNDkuMDI2MSA1NC4yMjM5IDQ5LjI1IDU0LjUgNDkuMjVDNTQuNzc2MSA0OS4yNSA1NSA0OS4wMjYxIDU1IDQ4Ljc1VjQ1LjI1QzU1IDQ0Ljk3MzkgNTQuNzc2MSA0NC43NSA1NC41IDQ0Ljc1QzU0LjIyMzkgNDQuNzUgNTQgNDQuOTczOSA1NCA0NS4yNVY0OC43NVpNNTQgMzkuNzVDNTQgNDAuMDI2MSA1NC4yMjM5IDQwLjI1IDU0LjUgNDAuMjVDNTQuNzc2MSA0MC4yNSA1NSA0MC4wMjYxIDU1IDM5Ljc1VjM2LjI1QzU1IDM1Ljk3MzkgNTQuNzc2MSAzNS43NSA1NC41IDM1Ljc1QzU0LjIyMzkgMzUuNzUgNTQgMzUuOTczOSA1NCAzNi4yNVYzOS43NVoiIGZpbGw9IiM4MDgwODAiLz4KPHBhdGggZD0iTTU0IDMwQzU0LjU1MjMgMzAgNTUgMjkuNTUyMyA1NSAyOUM1NSAyOC40NDc3IDU0LjU1MjMgMjggNTQgMjhDNTMuNDQ3NyAyOCA1MyAyOC40NDc3IDUzIDI5QzUzIDI5LjU1MjMgNTMuNDQ3NyAzMCA1NCAzMFoiIGZpbGw9IiM4MDgwODAiLz4KPHBhdGggZD0iTTgxIDMwQzgxLjU1MiAzMCA4MiAyOS41NTIzIDgyIDI5QzgyIDI4LjQ0NzcgODEuNTUyIDI4IDgxIDI4QzgwLjQ0OCAyOCA4MCAyOC40NDc3IDgwIDI5QzgwIDI5LjU1MjMgODAuNDQ4IDMwIDgxIDMwWiIgZmlsbD0iIzgwODA4MCIvPgo8cGF0aCBkPSJNNTQgNTdDNTQuNTUyMyA1NyA1NSA1Ni41NTIzIDU1IDU2QzU1IDU1LjQ0NzcgNTQuNTUyMyA1NSA1NCA1NUM1My40NDc3IDU1IDUzIDU1LjQ0NzcgNTMgNTZDNTMgNTYuNTUyMyA1My40NDc3IDU3IDU0IDU3WiIgZmlsbD0iIzgwODA4MCIvPgo8cGF0aCBkPSJNODEgNTdDODEuNTUyIDU3IDgyIDU2LjU1MjMgODIgNTZDODIgNTUuNDQ3NyA4MS41NTIgNTUgODEgNTVDODAuNDQ4IDU1IDgwIDU1LjQ0NzcgODAgNTZDODAgNTYuNTUyMyA4MC40NDggNTcgODEgNTdaIiBmaWxsPSIjODA4MDgwIi8+CjxwYXRoIGQ9Ik03NS40MDU5IDUySDYwLjU5NDJDNTkuMTcxNiA1MiA1OCA1MC44OTE3IDU4IDQ5LjU0NThWMzUuNDU0MkM1OCAzNC4xMDgzIDU5LjE3MTYgMzMgNjAuNTk0MiAzM0g3NS40MDU5Qzc3LjUgMzMgNzggMzQuMTA4MyA3OCAzNS40NTQyVjQ5LjQ2NjdDNzggNTEgNzcgNTIgNzUuNDA1OSA1MloiIGZpbGw9IndoaXRlIi8+CjxwYXRoIGZpbGwtcnVsZT0iZXZlbm9kZCIgY2xpcC1ydWxlPSJldmVub2RkIiBkPSJNNjAuNTA1OCAzM0M1OS41NTI2IDMzIDU4IDM0LjQyNzIgNTggMzUuMzMzNlY0OS42NjY0QzU4IDUxIDU5IDUyIDYwLjUwNTggNTJINzUuNDk0MkM3NyA1MiA3OCA1MSA3OCA0OS41VjM1LjMzMzZDNzggMzQgNzcgMzMgNzUuNDk0MiAzM0g2MC41MDU4Wk01NyAzNS4zMzM2QzU3IDMzLjUwMjIgNTguNTc5OSAzMiA2MC41MDU4IDMySDc1LjQ5NDJDNzcuNDIgMzIgNzkgMzMuNTAyMiA3OSAzNS4zMzM2VjQ5LjU4NTlDNzkgNTEuNDgwMyA3Ny40MzggNTMgNzUuNDk0MiA1M0g2MC41MDU4QzU4LjU3OTkgNTMgNTcgNTEuNDk3OCA1NyA0OS42NjY0VjM1LjMzMzZaIiBmaWxsPSIjNDQ0NDQ0Ii8+CjxwYXRoIGQ9Ik02MyA0MEM2MyAzOS40NDc3IDYyLjU1MjMgMzkgNjIgMzlDNjEuNDQ3NyAzOSA2MSAzOS40NDc3IDYxIDQwVjQ4QzYxIDQ4LjU1MjMgNjEuNDQ3NyA0OSA2MiA0OUM2Mi41NTIzIDQ5IDYzIDQ4LjU1MjMgNjMgNDhWNDBaIiBmaWxsPSIjRkY2RjNEIi8+CjxwYXRoIGQ9Ik02NyA0NEM2NyA0My40NDc3IDY2LjU1MjMgNDMgNjYgNDNDNjUuNDQ3NyA0MyA2NSA0My40NDc3IDY1IDQ0VjQ4QzY1IDQ4LjU1MjMgNjUuNDQ3NyA0OSA2NiA0OUM2Ni41NTIzIDQ5IDY3IDQ4LjU1MjMgNjcgNDhWNDRaIiBmaWxsPSIjRkY2RjNEIi8+CjxwYXRoIGQ9Ik03MSA0MkM3MSA0MS40NDc3IDcwLjU1MjMgNDEgNzAgNDFDNjkuNDQ3NyA0MSA2OSA0MS40NDc3IDY5IDQyVjQ4QzY5IDQ4LjU1MjMgNjkuNDQ3NyA0OSA3MCA0OUM3MC41NTIzIDQ5IDcxIDQ4LjU1MjMgNzEgNDhWNDJaIiBmaWxsPSIjRkY2RjNEIi8+CjxwYXRoIGQ9Ik03NSAzN0M3NSAzNi40NDc3IDc0LjU1MjMgMzYgNzQgMzZDNzMuNDQ3NyAzNiA3MyAzNi40NDc3IDczIDM3VjQ4QzczIDQ4LjU1MjMgNzMuNDQ3NyA0OSA3NCA0OUM3NC41NTIzIDQ5IDc1IDQ4LjU1MjMgNzUgNDhWMzdaIiBmaWxsPSIjRkY2RjNEIi8+CjxwYXRoIGQ9Ik04MCA2MUg0M1Y3N0g4MFY2MVoiIGZpbGw9IndoaXRlIi8+CjxwYXRoIGZpbGwtcnVsZT0iZXZlbm9kZCIgY2xpcC1ydWxlPSJldmVub2RkIiBkPSJNNDIuNDg3NSA2M0M0Mi4yMTgzIDYzIDQyIDYyLjc4MTcgNDIgNjIuNTEyNVY2MUM0MiA2MC40NDc3IDQyLjQ0NzcgNjAgNDMgNjBINDQuNDI1QzQ0LjcwMTEgNjAgNDQuOTI1IDYwLjIyMzkgNDQuOTI1IDYwLjVDNDQuOTI1IDYwLjc3NjEgNDQuNzAxMSA2MSA0NC40MjUgNjFINDIuOTc1VjYyLjUxMjVDNDIuOTc1IDYyLjc4MTcgNDIuNzU2NyA2MyA0Mi40ODc1IDYzWk01NC4xODc1IDYwLjVDNTQuMTg3NSA2MC4yMjM5IDUzLjk2MzYgNjAgNTMuNjg3NSA2MEg0OS44MTI1QzQ5LjUzNjQgNjAgNDkuMzEyNSA2MC4yMjM5IDQ5LjMxMjUgNjAuNUM0OS4zMTI1IDYwLjc3NjEgNDkuNTM2NCA2MSA0OS44MTI1IDYxSDUzLjY4NzVDNTMuOTYzNiA2MSA1NC4xODc1IDYwLjc3NjEgNTQuMTg3NSA2MC41Wk01OS41NjI1IDYwQzU5LjI4NjQgNjAgNTkuMDYyNSA2MC4yMjM5IDU5LjA2MjUgNjAuNUM1OS4wNjI1IDYwLjc3NjEgNTkuMjg2NCA2MSA1OS41NjI1IDYxSDYzLjQzNzVDNjMuNzEzNiA2MSA2My45Mzc1IDYwLjc3NjEgNjMuOTM3NSA2MC41QzYzLjkzNzUgNjAuMjIzOSA2My43MTM2IDYwIDYzLjQzNzUgNjBINTkuNTYyNVpNNjkuMzEyNSA2MEM2OS4wMzY0IDYwIDY4LjgxMjUgNjAuMjIzOSA2OC44MTI1IDYwLjVDNjguODEyNSA2MC43NzYxIDY5LjAzNjQgNjEgNjkuMzEyNSA2MUg3My4xODc1QzczLjQ2MzYgNjEgNzMuNjg3NSA2MC43NzYxIDczLjY4NzUgNjAuNUM3My42ODc1IDYwLjIyMzkgNzMuNDYzNiA2MCA3My4xODc1IDYwSDY5LjMxMjVaTTc4LjU3NSA2MEM3OC4yOTkgNjAgNzguMDc1IDYwLjIyMzkgNzguMDc1IDYwLjVDNzguMDc1IDYwLjc3NjEgNzguMjk5IDYxIDc4LjU3NSA2MUg4MC4wMjVWNjIuNTEyNUM4MC4wMjUgNjIuNzgxNyA4MC4yNDMgNjMgODAuNTEyIDYzQzgwLjc4MiA2MyA4MSA2Mi43ODE3IDgxIDYyLjUxMjVWNjFDODEgNjAuNDQ3NyA4MC41NTIgNjAgODAgNjBINzguNTc1Wk04MSA2Ny4yMzc1QzgxIDY2Ljk2ODMgODAuNzgyIDY2Ljc1IDgwLjUxMiA2Ni43NUM4MC4yNDMgNjYuNzUgODAuMDI1IDY2Ljk2ODMgODAuMDI1IDY3LjIzNzVWNzAuNzYyNUM4MC4wMjUgNzEuMDMxNyA4MC4yNDMgNzEuMjUgODAuNTEyIDcxLjI1QzgwLjc4MiA3MS4yNSA4MSA3MS4wMzE3IDgxIDcwLjc2MjVWNjcuMjM3NVpNODEgNzUuNDg4QzgxIDc1LjIxOCA4MC43ODIgNzUgODAuNTEyIDc1QzgwLjI0MyA3NSA4MC4wMjUgNzUuMjE4IDgwLjAyNSA3NS40ODhWNzdINzguNTc1Qzc4LjI5OSA3NyA3OC4wNzUgNzcuMjI0IDc4LjA3NSA3Ny41Qzc4LjA3NSA3Ny43NzYgNzguMjk5IDc4IDc4LjU3NSA3OEg4MEM4MC41NTIgNzggODEgNzcuNTUyIDgxIDc3Vjc1LjQ4OFpNNzMuMTg3NSA3OEM3My40NjM2IDc4IDczLjY4NzUgNzcuNzc2IDczLjY4NzUgNzcuNUM3My42ODc1IDc3LjIyNCA3My40NjM2IDc3IDczLjE4NzUgNzdINjkuMzEyNUM2OS4wMzY0IDc3IDY4LjgxMjUgNzcuMjI0IDY4LjgxMjUgNzcuNUM2OC44MTI1IDc3Ljc3NiA2OS4wMzY0IDc4IDY5LjMxMjUgNzhINzMuMTg3NVpNNjMuNDM3NSA3OEM2My43MTM2IDc4IDYzLjkzNzUgNzcuNzc2IDYzLjkzNzUgNzcuNUM2My45Mzc1IDc3LjIyNCA2My43MTM2IDc3IDYzLjQzNzUgNzdINTkuNTYyNUM1OS4yODY0IDc3IDU5LjA2MjUgNzcuMjI0IDU5LjA2MjUgNzcuNUM1OS4wNjI1IDc3Ljc3NiA1OS4yODY0IDc4IDU5LjU2MjUgNzhINjMuNDM3NVpNNTMuNjg3NSA3OEM1My45NjM2IDc4IDU0LjE4NzUgNzcuNzc2IDU0LjE4NzUgNzcuNUM1NC4xODc1IDc3LjIyNCA1My45NjM2IDc3IDUzLjY4NzUgNzdINDkuODEyNUM0OS41MzY0IDc3IDQ5LjMxMjUgNzcuMjI0IDQ5LjMxMjUgNzcuNUM0OS4zMTI1IDc3Ljc3NiA0OS41MzY0IDc4IDQ5LjgxMjUgNzhINTMuNjg3NVpNNDQuNDI1IDc4QzQ0LjcwMTEgNzggNDQuOTI1IDc3Ljc3NiA0NC45MjUgNzcuNUM0NC45MjUgNzcuMjI0IDQ0LjcwMTEgNzcgNDQuNDI1IDc3SDQyLjk3NVY3NS40ODhDNDIuOTc1IDc1LjIxOCA0Mi43NTY3IDc1IDQyLjQ4NzUgNzVDNDIuMjE4MyA3NSA0MiA3NS4yMTggNDIgNzUuNDg4Vjc3QzQyIDc3LjU1MiA0Mi40NDc3IDc4IDQzIDc4SDQ0LjQyNVpNNDIgNzAuNzYyNUM0MiA3MS4wMzE3IDQyLjIxODMgNzEuMjUgNDIuNDg3NSA3MS4yNUM0Mi43NTY3IDcxLjI1IDQyLjk3NSA3MS4wMzE3IDQyLjk3NSA3MC43NjI1VjY3LjIzNzVDNDIuOTc1IDY2Ljk2ODMgNDIuNzU2NyA2Ni43NSA0Mi40ODc1IDY2Ljc1QzQyLjIxODMgNjYuNzUgNDIgNjYuOTY4MyA0MiA2Ny4yMzc1VjcwLjc2MjVaIiBmaWxsPSIjODA4MDgwIi8+CjxwYXRoIGQ9Ik00MiA2MUM0Mi41NTIzIDYxIDQzIDYwLjU1MjMgNDMgNjBDNDMgNTkuNDQ3NyA0Mi41NTIzIDU5IDQyIDU5QzQxLjQ0NzcgNTkgNDEgNTkuNDQ3NyA0MSA2MEM0MSA2MC41NTIzIDQxLjQ0NzcgNjEgNDIgNjFaIiBmaWxsPSIjODA4MDgwIi8+CjxwYXRoIGQ9Ik04MSA2MUM4MS41NTIgNjEgODIgNjAuNTUyMyA4MiA2MEM4MiA1OS40NDc3IDgxLjU1MiA1OSA4MSA1OUM4MC40NDggNTkgODAgNTkuNDQ3NyA4MCA2MEM4MCA2MC41NTIzIDgwLjQ0OCA2MSA4MSA2MVoiIGZpbGw9IiM4MDgwODAiLz4KPHBhdGggZD0iTTQyIDc5QzQyLjU1MjMgNzkgNDMgNzguNTUyIDQzIDc4QzQzIDc3LjQ0OCA0Mi41NTIzIDc3IDQyIDc3QzQxLjQ0NzcgNzcgNDEgNzcuNDQ4IDQxIDc4QzQxIDc4LjU1MiA0MS40NDc3IDc5IDQyIDc5WiIgZmlsbD0iIzgwODA4MCIvPgo8cGF0aCBkPSJNODEgNzlDODEuNTUyIDc5IDgyIDc4LjU1MiA4MiA3OEM4MiA3Ny40NDggODEuNTUyIDc3IDgxIDc3QzgwLjQ0OCA3NyA4MCA3Ny40NDggODAgNzhDODAgNzguNTUyIDgwLjQ0OCA3OSA4MSA3OVoiIGZpbGw9IiM4MDgwODAiLz4KPHBhdGggZD0iTTUyIDY0SDQ3QzQ2LjQ0NzcgNjQgNDYgNjQuNDQ3NyA0NiA2NUM0NiA2NS41NTIzIDQ2LjQ0NzcgNjYgNDcgNjZINTJDNTIuNTUyMyA2NiA1MyA2NS41NTIzIDUzIDY1QzUzIDY0LjQ0NzcgNTIuNTUyMyA2NCA1MiA2NFoiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTUyIDY4SDQ3QzQ2LjQ0NzcgNjggNDYgNjguNDQ3NyA0NiA2OUM0NiA2OS41NTIzIDQ2LjQ0NzcgNzAgNDcgNzBINTJDNTIuNTUyMyA3MCA1MyA2OS41NTIzIDUzIDY5QzUzIDY4LjQ0NzcgNTIuNTUyMyA2OCA1MiA2OFoiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTUyIDcySDQ3QzQ2LjQ0NzcgNzIgNDYgNzIuNDQ3NyA0NiA3M0M0NiA3My41NTIzIDQ2LjQ0NzcgNzQgNDcgNzRINTJDNTIuNTUyMyA3NCA1MyA3My41NTIzIDUzIDczQzUzIDcyLjQ0NzcgNTIuNTUyMyA3MiA1MiA3MloiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTYwIDY0SDU1QzU0LjQ0NzcgNjQgNTQgNjQuNDQ3NyA1NCA2NUM1NCA2NS41NTIzIDU0LjQ0NzcgNjYgNTUgNjZINjBDNjAuNTUyMyA2NiA2MSA2NS41NTIzIDYxIDY1QzYxIDY0LjQ0NzcgNjAuNTUyMyA2NCA2MCA2NFoiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTYwIDY4SDU1QzU0LjQ0NzcgNjggNTQgNjguNDQ3NyA1NCA2OUM1NCA2OS41NTIzIDU0LjQ0NzcgNzAgNTUgNzBINjBDNjAuNTUyMyA3MCA2MSA2OS41NTIzIDYxIDY5QzYxIDY4LjQ0NzcgNjAuNTUyMyA2OCA2MCA2OFoiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTYwIDcySDU1QzU0LjQ0NzcgNzIgNTQgNzIuNDQ3NyA1NCA3M0M1NCA3My41NTIzIDU0LjQ0NzcgNzQgNTUgNzRINjBDNjAuNTUyMyA3NCA2MSA3My41NTIzIDYxIDczQzYxIDcyLjQ0NzcgNjAuNTUyMyA3MiA2MCA3MloiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTY4IDY0SDYzQzYyLjQ0NzcgNjQgNjIgNjQuNDQ3NyA2MiA2NUM2MiA2NS41NTIzIDYyLjQ0NzcgNjYgNjMgNjZINjhDNjguNTUyMyA2NiA2OSA2NS41NTIzIDY5IDY1QzY5IDY0LjQ0NzcgNjguNTUyMyA2NCA2OCA2NFoiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTY4IDY4SDYzQzYyLjQ0NzcgNjggNjIgNjguNDQ3NyA2MiA2OUM2MiA2OS41NTIzIDYyLjQ0NzcgNzAgNjMgNzBINjhDNjguNTUyMyA3MCA2OSA2OS41NTIzIDY5IDY5QzY5IDY4LjQ0NzcgNjguNTUyMyA2OCA2OCA2OFoiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTY4IDcySDYzQzYyLjQ0NzcgNzIgNjIgNzIuNDQ3NyA2MiA3M0M2MiA3My41NTIzIDYyLjQ0NzcgNzQgNjMgNzRINjhDNjguNTUyMyA3NCA2OSA3My41NTIzIDY5IDczQzY5IDcyLjQ0NzcgNjguNTUyMyA3MiA2OCA3MloiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTc2IDY0SDcxQzcwLjQ0NzcgNjQgNzAgNjQuNDQ3NyA3MCA2NUM3MCA2NS41NTIzIDcwLjQ0NzcgNjYgNzEgNjZINzZDNzYuNTUyMyA2NiA3NyA2NS41NTIzIDc3IDY1Qzc3IDY0LjQ0NzcgNzYuNTUyMyA2NCA3NiA2NFoiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTc2IDY4SDcxQzcwLjQ0NzcgNjggNzAgNjguNDQ3NyA3MCA2OUM3MCA2OS41NTIzIDcwLjQ0NzcgNzAgNzEgNzBINzZDNzYuNTUyMyA3MCA3NyA2OS41NTIzIDc3IDY5Qzc3IDY4LjQ0NzcgNzYuNTUyMyA2OCA3NiA2OFoiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTc2IDcySDcxQzcwLjQ0NzcgNzIgNzAgNzIuNDQ3NyA3MCA3M0M3MCA3My41NTIzIDcwLjQ0NzcgNzQgNzEgNzRINzZDNzYuNTUyMyA3NCA3NyA3My41NTIzIDc3IDczQzc3IDcyLjQ0NzcgNzYuNTUyMyA3MiA3NiA3MloiIGZpbGw9IiNCREVDRkYiLz4KPHBhdGggZD0iTTM2IDQ4SDJWNzJIMzZWNDhaIiBmaWxsPSJ3aGl0ZSIvPgo8cGF0aCBkPSJNMjkuNjM2NCA1Mkg4LjM2MzZDNy4wMjQyIDUyIDYgNTIuOTkwNSA2IDU0LjI4NTdWNjUuNzE0M0M2IDY3LjAwOTUgNy4wMjQyIDY4IDguMzYzNiA2OEgyOS42MzY0QzMwLjk3NTggNjggMzIgNjcuMDA5NSAzMiA2NS43MTQzVjU0LjI4NTdDMzIgNTIuOTkwNSAzMC45NzU4IDUyIDI5LjYzNjQgNTJaIiBmaWxsPSJ3aGl0ZSIvPgo8cGF0aCBkPSJNMjAuNTc1OCA2NC40NDgzTDE0LjI3MjcgNTdMNiA2Ni41MTcyQzYgNjcuOTI0MSA3LjAyNDIgNjkgOC4zNjM2IDY5SDI5LjYzNjRDMzAuOTc1OCA2OSAzMiA2Ny45MjQxIDMyIDY2LjUxNzJMMjUuMzAzIDU5LjQ4MjhMMjAuNTc1OCA2NC40NDgzWiIgZmlsbD0iI0ZGNkYzRCIvPgo8cGF0aCBkPSJNMjEgNThDMjIuMTA0NiA1OCAyMyA1Ny4xMDQ2IDIzIDU2QzIzIDU0Ljg5NTQgMjIuMTA0NiA1NCAyMSA1NEMxOS44OTU0IDU0IDE5IDU0Ljg5NTQgMTkgNTZDMTkgNTcuMTA0NiAxOS44OTU0IDU4IDIxIDU4WiIgZmlsbD0iI0ZGNkYzRCIvPgo8cGF0aCBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGNsaXAtcnVsZT0iZXZlbm9kZCIgZD0iTTUgNTQuMTUwOEM1IDUyLjM3NyA2LjQwOTMgNTEgOC4yMjQ2IDUxSDI5Ljc3NTRDMzEuNTkwNyA1MSAzMyA1Mi4zNzcgMzMgNTQuMTUwOFY2NS44NDkyQzMzIDY3LjYyMyAzMS41OTA3IDY5IDI5Ljc3NTQgNjlIOC4yMjQ2QzYuNDA5MyA2OSA1IDY3LjYyMyA1IDY1Ljg0OTJWNTQuMTUwOFpNOC4yMjQ2IDUyQzcgNTIgNiA1MyA2IDU0LjE1MDhWNjUuODQ5MkM2IDY3IDcgNjggOC4yMjQ2IDY4SDI5Ljc3NTRDMzEgNjggMzIgNjcgMzIgNjUuODQ5MlY1NC4xNTA4QzMyIDUzIDMxIDUyIDI5Ljc3NTQgNTJIOC4yMjQ2WiIgZmlsbD0iIzQ0NDQ0NCIvPgo8cGF0aCBkPSJNMSA0OEMxLjU1MjMgNDggMiA0Ny41NTIzIDIgNDdDMiA0Ni40NDc3IDEuNTUyMyA0NiAxIDQ2QzAuNDQ3NyA0NiAwIDQ2LjQ0NzcgMCA0N0MwIDQ3LjU1MjMgMC40NDc3IDQ4IDEgNDhaIiBmaWxsPSIjODA4MDgwIi8+CjxwYXRoIGQ9Ik0zNyA0OEMzNy41NTIzIDQ4IDM4IDQ3LjU1MjMgMzggNDdDMzggNDYuNDQ3NyAzNy41NTIzIDQ2IDM3IDQ2QzM2LjQ0NzcgNDYgMzYgNDYuNDQ3NyAzNiA0N0MzNiA0Ny41NTIzIDM2LjQ0NzcgNDggMzcgNDhaIiBmaWxsPSIjODA4MDgwIi8+CjxwYXRoIGQ9Ik0xIDc0QzEuNTUyMyA3NCAyIDczLjU1MjMgMiA3M0MyIDcyLjQ0NzcgMS41NTIzIDcyIDEgNzJDMC40NDc3IDcyIDAgNzIuNDQ3NyAwIDczQzAgNzMuNTUyMyAwLjQ0NzcgNzQgMSA3NFoiIGZpbGw9IiM4MDgwODAiLz4KPHBhdGggZD0iTTM3IDc0QzM3LjU1MjMgNzQgMzggNzMuNTUyMyAzOCA3M0MzOCA3Mi40NDc3IDM3LjU1MjMgNzIgMzcgNzJDMzYuNDQ3NyA3MiAzNiA3Mi40NDc3IDM2IDczQzM2IDczLjU1MjMgMzYuNDQ3NyA3NCAzNyA3NFoiIGZpbGw9IiM4MDgwODAiLz4KPHBhdGggZmlsbC1ydWxlPSJldmVub2RkIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik0xLjUyOTQgNTAuMTJDMS4yMzcgNTAuMTIgMSA0OS44ODMgMSA0OS41OTA2VjQ4QzEgNDcuNDQ3NyAxLjQ0NzcgNDcgMiA0N0gzLjQ4QzMuNzY3MiA0NyA0IDQ3LjIzMjggNCA0Ny41MkM0IDQ3LjgwNzIgMy43NjcyIDQ4LjA0IDMuNDggNDguMDRIMi4wNTg4VjQ5LjU5MDZDMi4wNTg4IDQ5Ljg4MyAxLjgyMTggNTAuMTIgMS41Mjk0IDUwLjEyWk0xMC41MiA0N0MxMC4yMzI4IDQ3IDEwIDQ3LjIzMjggMTAgNDcuNTJDMTAgNDcuODA3MiAxMC4yMzI4IDQ4LjA0IDEwLjUyIDQ4LjA0SDE1LjQ4QzE1Ljc2NzIgNDguMDQgMTYgNDcuODA3MiAxNiA0Ny41MkMxNiA0Ny4yMzI4IDE1Ljc2NzIgNDcgMTUuNDggNDdIMTAuNTJaTTIyLjUyIDQ3QzIyLjIzMjggNDcgMjIgNDcuMjMyOCAyMiA0Ny41MkMyMiA0Ny44MDcyIDIyLjIzMjggNDguMDQgMjIuNTIgNDguMDRIMjcuNDhDMjcuNzY3MiA0OC4wNCAyOCA0Ny44MDcyIDI4IDQ3LjUyQzI4IDQ3LjIzMjggMjcuNzY3MiA0NyAyNy40OCA0N0gyMi41MlpNMzQuNTIgNDdDMzQuMjMyOCA0NyAzNCA0Ny4yMzI4IDM0IDQ3LjUyQzM0IDQ3LjgwNzIgMzQuMjMyOCA0OC4wNCAzNC41MiA0OC4wNEgzNS45NDEyVjQ5LjU5MDZDMzUuOTQxMiA0OS44ODMgMzYuMTc4MiA1MC4xMiAzNi40NzA2IDUwLjEyQzM2Ljc2MyA1MC4xMiAzNyA0OS44ODMgMzcgNDkuNTkwNlY0OEMzNyA0Ny40NDc3IDM2LjU1MjMgNDcgMzYgNDdIMzQuNTJaTTM2LjQ3MDYgNTcuODMzM0MzNi43NjMgNTcuODMzMyAzNyA1Ny41OTYzIDM3IDU3LjMwMzlWNTQuMDI5NEMzNyA1My43MzcgMzYuNzYzIDUzLjUgMzYuNDcwNiA1My41QzM2LjE3ODIgNTMuNSAzNS45NDEyIDUzLjczNyAzNS45NDEyIDU0LjAyOTRWNTcuMzAzOUMzNS45NDEyIDU3LjU5NjMgMzYuMTc4MiA1Ny44MzMzIDM2LjQ3MDYgNTcuODMzM1pNMzcgNjIuNjk2MUMzNyA2Mi40MDM3IDM2Ljc2MyA2Mi4xNjY3IDM2LjQ3MDYgNjIuMTY2N0MzNi4xNzgyIDYyLjE2NjcgMzUuOTQxMiA2Mi40MDM3IDM1Ljk0MTIgNjIuNjk2MVY2NS45NzA2QzM1Ljk0MTIgNjYuMjYzIDM2LjE3ODIgNjYuNSAzNi40NzA2IDY2LjVDMzYuNzYzIDY2LjUgMzcgNjYuMjYzIDM3IDY1Ljk3MDZWNjIuNjk2MVpNMzcgNzAuNDA5NEMzNyA3MC4xMTcgMzYuNzYzIDY5Ljg4IDM2LjQ3MDYgNjkuODhDMzYuMTc4MiA2OS44OCAzNS45NDEyIDcwLjExNyAzNS45NDEyIDcwLjQwOTRWNzEuOTZIMzQuNTJDMzQuMjMyOCA3MS45NiAzNCA3Mi4xOTI4IDM0IDcyLjQ4QzM0IDcyLjc2NzIgMzQuMjMyOCA3MyAzNC41MiA3M0gzNkMzNi41NTIzIDczIDM3IDcyLjU1MjMgMzcgNzJWNzAuNDA5NFpNMjcuNDggNzNDMjcuNzY3MiA3MyAyOCA3Mi43NjcyIDI4IDcyLjQ4QzI4IDcyLjE5MjggMjcuNzY3MiA3MS45NiAyNy40OCA3MS45NkgyMi41MkMyMi4yMzI4IDcxLjk2IDIyIDcyLjE5MjggMjIgNzIuNDhDMjIgNzIuNzY3MiAyMi4yMzI4IDczIDIyLjUyIDczSDI3LjQ4Wk0xNS40OCA3M0MxNS43NjcyIDczIDE2IDcyLjc2NzIgMTYgNzIuNDhDMTYgNzIuMTkyOCAxNS43NjcyIDcxLjk2IDE1LjQ4IDcxLjk2SDEwLjUyQzEwLjIzMjggNzEuOTYgMTAgNzIuMTkyOCAxMCA3Mi40OEMxMCA3Mi43NjcyIDEwLjIzMjggNzMgMTAuNTIgNzNIMTUuNDhaTTMuNDggNzNDMy43NjcyIDczIDQgNzIuNzY3MiA0IDcyLjQ4QzQgNzIuMTkyOCAzLjc2NzIgNzEuOTYgMy40OCA3MS45NkgyLjA1ODhWNzAuNDA5NEMyLjA1ODggNzAuMTE3IDEuODIxOCA2OS44OCAxLjUyOTQgNjkuODhDMS4yMzcgNjkuODggMSA3MC4xMTcgMSA3MC40MDk0VjcyQzEgNzIuNTUyMyAxLjQ0NzcgNzMgMiA3M0gzLjQ4Wk0xIDY1Ljk3MDZDMSA2Ni4yNjMgMS4yMzcgNjYuNSAxLjUyOTQgNjYuNUMxLjgyMTggNjYuNSAyLjA1ODggNjYuMjYzIDIuMDU4OCA2NS45NzA2VjYyLjY5NjFDMi4wNTg4IDYyLjQwMzcgMS44MjE4IDYyLjE2NjcgMS41Mjk0IDYyLjE2NjdDMS4yMzcgNjIuMTY2NyAxIDYyLjQwMzcgMSA2Mi42OTYxVjY1Ljk3MDZaTTEgNTcuMzAzOUMxIDU3LjU5NjMgMS4yMzcgNTcuODMzMyAxLjUyOTQgNTcuODMzM0MxLjgyMTggNTcuODMzMyAyLjA1ODggNTcuNTk2MyAyLjA1ODggNTcuMzAzOVY1NC4wMjk0QzIuMDU4OCA1My43MzcgMS44MjE4IDUzLjUgMS41Mjk0IDUzLjVDMS4yMzcgNTMuNSAxIDUzLjczNyAxIDU0LjAyOTRWNTcuMzAzOVoiIGZpbGw9IiM4MDgwODAiLz4KPHBhdGggZD0iTTQyIDcuMjA4N0M0MiA3LjQ3NTcgNDEuOTM3NSA3LjczODcgNDEuODE4IDcuOTc0N0w0MC43MjU1IDEwLjEzMTdDNDAuNDU0OSAxMC42NjYgMzkuOTI0OCAxMSAzOS4zNDc1IDExSDM4LjY1MjVDMzguMDc1MiAxMSAzNy41NDUxIDEwLjY2NiAzNy4yNzQ1IDEwLjEzMTdMMzYuMTgyIDcuOTc0N0MzNi4wNjI1IDcuNzM4NyAzNiA3LjQ3NTcgMzYgNy4yMDg3VjEuNjM0M0MzNiAwLjczMTcgMzYuNjk4NCAwIDM3LjU2IDBINDAuNDRDNDEuMzAxNiAwIDQyIDAuNzMxNyA0MiAxLjYzNDNWNy4yMDg3WiIgZmlsbD0iI0VGRUZFRiIvPgo8cGF0aCBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGNsaXAtcnVsZT0iZXZlbm9kZCIgZD0iTTM5LjM0NzUgMTAuMTMxN0w0MSA3LjIwODdWMUgzN1Y3LjIwODdMMzguNjUyNSAxMC4xMzE3SDM5LjM0NzVaTTQxLjgxOCA3Ljk3NDdDNDEuOTM3NSA3LjczODcgNDIgNy40NzU3IDQyIDcuMjA4N1YxLjYzNDNDNDIgMC43MzE3IDQxLjMwMTYgMCA0MC40NCAwSDM3LjU2QzM2LjY5ODQgMCAzNiAwLjczMTcgMzYgMS42MzQzVjcuMjA4N0MzNiA3LjQ3NTcgMzYuMDYyNSA3LjczODcgMzYuMTgyIDcuOTc0N0wzNy4yNzQ1IDEwLjEzMTdDMzcuNTQ1MSAxMC42NjYgMzguMDc1MiAxMSAzOC42NTI1IDExSDM5LjM0NzVDMzkuOTI0OCAxMSA0MC40NTQ5IDEwLjY2NiA0MC43MjU1IDEwLjEzMTdMNDEuODE4IDcuOTc0N1oiIGZpbGw9IiM0NDQ0NDQiLz4KPC9zdmc+Cg==");
        }

        #onlyoffice-cloud-banner h3 {
            border-bottom: none;
            font-size: 20px;
            font-weight: 600;
            line-height: 27px;
            letter-spacing: 0em;
            margin: 10px 0;
            padding: 0px;
            text-align: left;
        }

        #onlyoffice-cloud-banner .info {
            box-sizing: border-box;
            display: inline-block;
            padding-left: 105px;
            vertical-align: middle;
            width: 100%;
        }

        #onlyoffice-cloud-banner .onlyoffice-cloud-banner-buttons {
            display: inline-block;
            box-sizing: border-box;
            padding: 15px;
            text-align: center;
            width: 29%;
        }

        #onlyoffice-cloud-banner .onlyoffice-cloud-banner-button {
            font-weight: 600;
            padding: 10px 5px;
            text-decoration: none;
            display: inline-block;
            width: 100%;
            box-sizing: border-box;
            background-color: #6e9e2d;
            color: #fff;
            border: 1px solid #777;
            cursor: pointer;
            border-radius: 4px;
        }
    </style>
</head>

</form>

<span data-settings-saved="${msg('onlyoffice-config.message.settings.saved')}" data-settings-saving-error="${msg('onlyoffice-config.message.settings.saving-error')}" data-mixedcontent="${msg('onlyoffice.server.common.error.mixed-content')}" data-apijs-unreachable="${msg('onlyoffice.server.common.error.api-js')}" data-onlyoffice-convert-service-prefix="${msg('onlyoffice.service.convert.check.error-prefix')}" data-onlyoffice-command-service-prefix="${msg('onlyoffice.service.command.check.error-prefix')}" id="onlyresponse" class="hidden"></span>

<div style="margin: 1em 0 0.666em;" id="messageBlock" class="hidden"></div>

<div class="column-left">
   <@section label=msg("onlyoffice-config.doc-section") />

   <form id="docservcfg" action="${url.service}" method="POST" accept-charset="utf-8">
      <div class="description section">${msg("onlyoffice-config.description")}</div>
      <div class="control text">
         <label class="label" for="onlyurl">${msg("onlyoffice-config.doc-url")}</label>
         <span class="value">
            <input id="onlyurl" name="url" size="35" placeholder="http://docserver/" title="${msg('onlyoffice-config.doc-url-tooltip')}" pattern="(http(s)?://.*)|(/.*)" value="${(settings['url'])!}" />
         </span>
      </div>
      <div class="control text">
         <label class="label" for="jwtsecret">${msg("onlyoffice-config.jwt-secret")}</label>
         <span class="value">
            <input class="value" id="jwtsecret" name="url" size="35" value="${(settings['security.key'])!}" />
         </span>
      </div>
      <div class="control text">
         <label class="label" for="securityHeader">${msg("onlyoffice-config.security.header")}</label>
         <span class="value">
            <input class="value" id="securityHeader" name="securityHeader" value="${(settings['security.header'])!}" />
         </span>
      </div>

      <@tsection label=msg("onlyoffice-config.advanced-section")>
         <div class="control text">
            <label class="label" for="onlyinnerurl">${msg("onlyoffice-config.doc-url-inner")}</label>
            <span class="value">
               <input class="value" id="onlyinnerurl" name="innerurl" size="35" placeholder="http://docserver/" title="${msg('onlyoffice-config.doc-url-inner-tooltip')}" pattern="http(s)?://.*" value="${(settings['innerUrl'])!}" />
            </span>
         </div>
         <div class="control text">
            <label class="label" for="alfurl">${msg("onlyoffice-config.alf-url")}</label>
            <span class="value">
               <input class="value" id="alfurl" name="alfurl" size="35" placeholder="http://alfresco/" title="${msg('onlyoffice-config.alf-url-tooltip')}" pattern="http(s)?://.*" value="${(settings['productInnerUrl'])!}" />
            </span>
         </div>
      </@tsection>

      <@section label=msg("onlyoffice-config.common-section") />
      <div class="control field">
         <input class="value" id="onlycert" name="cert" type="checkbox" <#if (settings['security.ignoreSSLCertificate'])! == 'true'>checked</#if> />
         <label class="label" for="onlycert">${msg("onlyoffice-config.ignore-ssl-cert")}</label>
      </div>
      <div class="control field">
         <input class="value" id="webpreview" name="cert" type="checkbox" <#if webpreview>checked</#if> />
         <label class="label" for="webpreview">${msg("onlyoffice-config.webpreview")}</label>
      </div>
      <div class="control field">
         <input class="value" id="convertOriginal" name="convertOriginal" type="checkbox" <#if convertOriginal>checked</#if> />
         <label class="label" for="convertOriginal">${msg("onlyoffice-config.convert-original")}</label>
      </div>
      <div class="control field section">
          <label class="label">${msg("onlyoffice-config.file-type")}</label>
          <div style="padding-top: 4px">
              <#list lossyEditable?keys as key>
                <div style="display: inline-block;">
                  <input class="value lossy-edit" id="${key}" name="${key}" type="checkbox" <#if lossyEditable[key]>checked="checked"</#if>/>
                  <label class="label" style="margin-right: 10px; width: 40px; display: inline-block" for="${key}">${key}</label>
                </div>
              </#list>
          </div>
      </div>

      <@section label=msg("onlyoffice-config.customization-section")/>
      <div class="control field">
          <input class="value" id="forcesave" name="forcesave" type="checkbox" <#if (settings['customization.forcesave'])! == 'true'>checked</#if> />
          <label class="label" for="forcesave">${msg("onlyoffice-config.forcesave")}</label>
      </div>
      <label class="control label">${msg("onlyoffice-config.customization-label")}</label>
      <div class="control field">
          <input class="value" id="chat" name="chat" type="checkbox" <#if (settings['customization.chat'])! == 'true'>checked</#if> />
          <label class="label" for="chat">${msg("onlyoffice-config.chat")}</label>
      </div>
      <div class="control field">
          <input class="value" id="compactHeader" name="compactHeader" type="checkbox" <#if (settings['customization.compactHeader'])! == 'true'>checked</#if> />
          <label class="label" for="compactHeader">${msg("onlyoffice-config.compact-header")}</label>
      </div>
      <div class="control field">
          <input class="value" id="feedback" name="feedback" type="checkbox" <#if (settings['customization.feedback'])! == 'true'>checked</#if> />
          <label class="label" for="feedback">${msg("onlyoffice-config.feedback")}</label>
      </div>
      <div class="control field">
          <input class="value" id="help" name="help" type="checkbox" <#if (settings['customization.help'])! == 'true'>checked</#if> />
          <label class="label" for="help">${msg("onlyoffice-config.help")}</label>
      </div>
      <div class="control field">
          <input class="value" id="toolbarNoTabs" name="toolbarNoTabs" type="checkbox" <#if (settings['customization.toolbarNoTabs'])! == 'true'>checked</#if> />
          <label class="label" for="toolbarNoTabs">${msg("onlyoffice-config.toolbar-no-tabs")}</label>
      </div>
      <div class="control field section">
          <p class="label">${msg("onlyoffice-config.review-mode-label")}</p>
          <div style="padding-top: 4px">
              <input class="value" id="reviewDisplayMarkup" name="reviewDisplay" type="radio" value="markup" <#if (settings['customization.review.reviewDisplay'])! == 'MARKUP'>checked</#if> />
              <label class="label" for="reviewDisplayMarkup" style="margin-right: 21px">${msg("onlyoffice-config.review-mode-markup")}</label>

              <input class="value" id="reviewDisplayFinal" name="reviewDisplay" type="radio" value="final" <#if (settings['customization.review.reviewDisplay'])! == 'FINAL'>checked</#if> />
              <label class="label" for="reviewDisplayFinal" style="margin-right: 21px">${msg("onlyoffice-config.review-mode-final")}</label>

              <input class="value" id="reviewDisplayOriginal" name="reviewDisplay" type="radio" value="original" <#if (settings['customization.review.reviewDisplay'])! == 'ORIGINAL'>checked</#if> />
              <label class="label" for="reviewDisplayOriginal" style="margin-right: 21px">${msg("onlyoffice-config.review-mode-original")}</label>
          </div>
      </div>

      <br>
      <table>
          <tr style="vertical-align: top;">
              <td>
                  <input id="postonlycfg" type="button" value="${msg('onlyoffice-config.save-btn')}"/>
              </td>
              <td>
                  <div class="control field" style="margin-left: 20px;">
                      <input class="value" id="onlyofficeDemo" name="onlyofficeDemo" type="checkbox" <#if (settings['demo'])! == 'true'>checked</#if> <#if !demoAvailable> disabled="disabled" </#if>/>
                      <label class="label" for="onlyofficeDemo">${msg("onlyoffice-config.demo-connect")}</label>
                      </br>
                      <#if demoAvailable>
                          <div class="description">${msg("onlyoffice-config.trial")}</div>
                      <#else>
                          <div class="description">${msg("onlyoffice-config.trial-is-over")}</div>
                      </#if>
                  </div>
              </td>
          </tr>
      </table>
   </form>

    <div id="onlyoffice-cloud-banner">
        <div class="onlyoffice-cloud-banner-info">
            <img>
            <div class="info">
                <h3>${msg("onlyoffice-config.banner.title")}</h3>
                <p>${msg("onlyoffice-config.banner.message")}</p>
            </div>
        </div>
        <div class="onlyoffice-cloud-banner-buttons">
            <a class="onlyoffice-cloud-banner-button" href="https://www.onlyoffice.com/docs-registration.aspx?referer=alfresco" target="_blank">${msg("onlyoffice-config.banner.button")}</a>
        </div>
    </div>
</div>

<script type="text/javascript">//<![CDATA[
   (function() {
      var url = document.getElementById("onlyurl");
      var innerurl = document.getElementById("onlyinnerurl");
      var alfurl = document.getElementById("alfurl");
      var cert = document.getElementById("onlycert");
      var fs = document.getElementById("forcesave");
      var webpreview = document.getElementById("webpreview");
      var convertOriginal = document.getElementById("convertOriginal");
      var jwts = document.getElementById("jwtsecret");
      var securityHeader = document.getElementById("securityHeader");
      var demo = document.getElementById("onlyofficeDemo");

      var form = document.getElementById("docservcfg");
      var btn = document.getElementById("postonlycfg");
      var msg = document.getElementById("onlyresponse");

      var chat = document.getElementById("chat");
      var help = document.getElementById("help");
      var compactHeader = document.getElementById("compactHeader");
      var toolbarNoTabs = document.getElementById("toolbarNoTabs");
      var feedback = document.getElementById("feedback");
      var lossyEdit = document.querySelectorAll(".lossy-edit");
      var reviewDisplay = document.getElementsByName("reviewDisplay");

      var doPost = function(obj) {
         var xhr = new XMLHttpRequest();
         xhr.open("POST", form.action, true);
         xhr.setRequestHeader("Content-type", "application/json");
         xhr.setRequestHeader("Accept", "application/json");
         xhr.overrideMimeType("application/json");

         xhr.onload = function () { savingCallback(xhr); };

         xhr.send(JSON.stringify(obj));
      };

      var savingCallback = function(xhr) {
         btn.disabled = false;

         if (xhr.status != 200) {
               showMessage(msg.dataset["settingsSavingError"], true);
               return;
         }

         var settingsValidationRequest = new XMLHttpRequest();
         settingsValidationRequest.open("GET", form.action + "-validation", true);
         settingsValidationRequest.setRequestHeader("Content-type", "application/json");
         settingsValidationRequest.setRequestHeader("Accept", "application/json");
         settingsValidationRequest.overrideMimeType("application/json");

         settingsValidationRequest.onload = function () {
            if (settingsValidationRequest.response) {
                const responseJson = JSON.parse(settingsValidationRequest.response);
                const validationResults = responseJson.validationResults;

                if (validationResults.documentServer) {
                    if (validationResults.documentServer.status == "failed") {
                        showMessage(validationResults.documentServer.message, true);
                    }
                }

                if (validationResults.commandService) {
                    if (validationResults.commandService.status == "failed") {
                        showMessage(
                            msg.dataset["onlyofficeCommandServicePrefix"].replace(
                                "$",
                                validationResults.commandService.message
                            ),
                            true
                        );
                    }
                }

                if (validationResults.convertService) {
                    if (validationResults.convertService.status == "failed") {
                        showMessage(
                            msg.dataset["onlyofficeConvertServicePrefix"].replace(
                                "$",
                                validationResults.convertService.message
                            ),
                            true
                        );
                    }
                }
            }

            showMessage(msg.dataset["settingsSaved"]);
         };

         settingsValidationRequest.send();
      };

      var parseForm = function() {
         var obj = {};

         obj.lossyEdit = [];

         lossyEdit.forEach((element) => {
            if (element.checked) obj.lossyEdit.push(element.id);
         });

         obj.url = url.value.trim();
         obj.innerUrl = innerurl.value.trim();
         obj.productInnerUrl = alfurl.value.trim();
         obj.security = {
            key: jwts.value.trim(),
            header: securityHeader.value.trim()
         };
         obj.ignoreSSLCertificate = cert.checked.toString();
         obj.demo = demo.checked.toString();
         obj.customization = {
            forcesave: fs.checked.toString(),
            feedback: feedback.checked.toString(),
            chat: chat.checked.toString(),
            help: help.checked.toString(),
            compactHeader: compactHeader.checked.toString(),
            toolbarNoTabs: toolbarNoTabs.checked.toString(),
            review: {
                reviewDisplay: document.querySelector("input[name='reviewDisplay']:checked").id.replace("reviewDisplay", "").toLowerCase()
            }
         };

         obj.convertOriginal = convertOriginal.checked.toString();
         obj.webpreview = webpreview.checked.toString();

         return obj;
      };

      var showMessage = function(message, error) {
        let messageBlock = document.getElementById("messageBlock");
        let messageElement = document.createElement("span");
        messageElement.classList.add('message');
        messageElement.style.width = "fit-content";
        messageElement.style.margin = "5px 0";
        messageElement.style.display = "block";
        messageElement.innerText = message;

        if (error) {
            messageElement.classList.add("error");
        }

        messageBlock.appendChild(messageElement);

        messageBlock.classList.remove("hidden");
      };

      var hideMessages = function() {
        document.getElementById("messageBlock").innerHTML = '';
      };

      var testDocServiceApi = function (obj) {
          var testApiResult = function () {
              var result = typeof DocsAPI != "undefined";

              if (!result) {
                showMessage(msg.dataset["apijsUnreachable"], true);
              }

              doPost(obj);
          };

          if (window.location.href.startsWith("https://") && obj.url.toLowerCase().startsWith("http://")) {
              btn.disabled = false;
              showMessage(msg.dataset["mixedcontent"], true);
              return;
          }

          delete DocsAPI;

          var scriptAddress = document.getElementById("scripDocServiceAddress");
          if (scriptAddress) scriptAddress.parentNode.removeChild(scriptAddress);

          var js = document.createElement("script");
          js.setAttribute("type", "text/javascript");
          js.setAttribute("id", "scripDocServiceAddress");
          document.getElementsByTagName("head")[0].appendChild(js);

          scriptAddress = document.getElementById("scripDocServiceAddress");

          scriptAddress.onload = testApiResult;
          scriptAddress.onerror = testApiResult;

          var docServiceUrlApi = obj.url;

          if (!docServiceUrlApi.endsWith("/")) {
              docServiceUrlApi += "/";
          }
          docServiceUrlApi += "web-apps/apps/api/documents/api.js";

          scriptAddress.src = docServiceUrlApi;
      };

      btn.onclick = function() {
         if (btn.disabled) return;

         btn.disabled = true;
         hideMessages();

         var obj = parseForm();

         if (demo.checked && !demo.disabled) {
            doPost(obj);
         } else {
            testDocServiceApi(obj);
         }
      };

      var demoToggle = function () {
          if (!demo.disabled) {
               url.disabled = demo.checked;
               jwts.disabled = demo.checked;
               innerurl.disabled = demo.checked;
               securityHeader.disabled = demo.checked;
          }
      };

      demo.onclick = demoToggle;
      demoToggle();
   })();
//]]></script>
</@page>
