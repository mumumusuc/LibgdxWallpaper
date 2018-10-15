import os

FILE_NAME = 'andy.obj'
MULTIPLIER = 20

if __name__ == '__main__':
    with open(FILE_NAME, 'r', encoding='utf-8') as f1, open('%s.bak' % FILE_NAME, 'w', encoding='utf-8') as f2:
        for line in f1:
            ws = str(line).split(' ')
            if ws[0] == 'v':
                _w1, _w2, _w3 = float(
                    ws[1])*MULTIPLIER, float(ws[2])*MULTIPLIER, float(ws[3])*MULTIPLIER
                line = 'v %.6f %.6f %.6f\r\n' % (_w1, _w2, _w3)
            f2.write(line)
