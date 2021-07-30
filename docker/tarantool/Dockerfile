FROM tarantool/tarantool:2.9.0

COPY *.lua /opt/tarantool
COPY schema/*.lua /opt/tarantool/schema/

CMD ["tarantool", "/opt/tarantool/app.lua"]