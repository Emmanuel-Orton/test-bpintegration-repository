import os
import json
import requests
from pathlib import Path
import sys

def body_file_path(self, relative_path):
    return os.path.join(self.steps_config.root_path, relative_path)

def main():
    client = requests.Session()
    client.verify = True
    target = sys.argv[1]
    print ('Retrieving request from :', target)

    response = client.get(f'http://{target}.dev.ts-op.be-delivery.com/mocking/__admin/requests')
    status = response.status_code
    if status == 200:
        response_body = response.json()
        req = response_body['requests']
        print(f"Found {len(req)} requests")
        if len(req) != 0:
            first_request = None
            for r in req:
                target_url = r['request']['url']
                if not target_url.startswith("/r6-api/") and not target_url.startswith("/r6-admin/") and r['request']['body']:
                    first_request = r
                    break
            if first_request is not None:
                print(f"Generating test for request {first_request['id']}")

                body_as_dict = json.loads(first_request['request']['body'])
                url = first_request['request']['url']


                print(body_as_dict)
                type_ = body_as_dict["eventType"]
                filename = type_ + ".json"
                file_path = os.path.join("requestPayloads")


                with open(f'{os.path.join(file_path, filename)}', 'w') as write_obj:
                    write_obj.write(first_request['request']['body'])

                print(f"""
                Add the following to a feature file:
                -------------------------------------------------

                @wip
                Scenario: An request from Infonova is retrieved
                    When an event of type "{type_}" is sent to url "{url}"
                """)
            else:
                print ("Did not find any suitable request to extract")


if __name__ == "__main__":
    main()
