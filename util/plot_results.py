from kafka import KafkaConsumer
import matplotlib.pyplot as plt
from decimal import Decimal

consumer = KafkaConsumer('dist-ga-chromosome-with-fitness', auto_offset_reset = 'earliest', value_deserializer=lambda m: m.decode('utf-8'))

y = []
x = []
i = 0

plt.ion()
ax = plt.gca()
ax.set_autoscale_on(True)
line, = ax.plot(x, y)

for msg in consumer:
    yn = Decimal(msg.value)
    y.append(yn)
    x.append(i)
    i = i + 1

    line.set_xdata(x)
    line.set_ydata(y)
    ax.relim()
    ax.autoscale_view(True,True,True)
    plt.draw()
    plt.pause(0.1)