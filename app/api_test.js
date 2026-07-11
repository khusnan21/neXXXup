const https = require('https');

const postData = 'r=&d=javplayers.com';

const options = {
  hostname: 'javplayers.com',
  port: 443,
  path: '/api/source/27dbb28ea03fd64ae84f717f6dfac59c',
  method: 'POST',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Content-Length': Buffer.byteLength(postData)
  }
};

const req = https.request(options, (res) => {
  let chunks = [];
  res.on('data', (d) => {
    chunks.push(d);
  });
  res.on('end', () => {
      console.log(Buffer.concat(chunks).toString());
  })
});

req.on('error', (e) => {
  console.error(e);
});

req.write(postData);
req.end();
