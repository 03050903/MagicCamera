#include "Conversion.h"

void Conversion::YCbCrToRGB(uint8_t* From, uint8_t* To, int Length)
{
	if (Length < 1) return;
	uint8_t* End = From + Length * 3;
	int Red, Green, Blue;
	int Y, Cb, Cr;
	while (From != End)
	{
		Y = *From; Cb = *(From + 1) - 128; Cr = *(From + 2) - 128;
		Red = Y + ((RGBRCrI * Cr + HalfShiftValue) >> Shift);
		Green = Y + ((RGBGCbI * Cb + RGBGCrI * Cr + HalfShiftValue) >> Shift);
		Blue = Y + ((RGBBCbI * Cb + HalfShiftValue) >> Shift);
		if (Red > 255) Red = 255; else if (Red < 0) Red = 0;
		if (Green > 255) Green = 255; else if (Green < 0) Green = 0;    // �����Ӧ�ñ���Ŀ�������Ч�ʸ�
		if (Blue > 255) Blue = 255; else if (Blue < 0) Blue = 0;
		*To = (uint8_t)Blue;                                               // ���ڲ���һһ��Ӧ�ģ���Ҫ�ж��Ƿ�Խ��
		*(To + 1) = (uint8_t)Green;
		*(To + 2) = (uint8_t)Red;
		*(To + 3) = 0xff;
		From += 3;
		To += 4;
	}
}

void Conversion::RGBToYCbCr(uint8_t* From, uint8_t* To, int Length)
{
	if (Length < 1) return;
	uint8_t* End = From + Length * 4;
	int Red, Green, Blue;
	while (From != End)
	{
		Blue = *From; Green = *(From + 1); Red = *(From + 2);
		// �����ж��Ƿ�����������Ϊ���Թ�����RGB�ռ��������ɫֵ������ɫ�������
		*To = (uint8_t)((YCbCrYRI * Red + YCbCrYGI * Green + YCbCrYBI * Blue + HalfShiftValue) >> Shift);
		*(To + 1) = (uint8_t)(128 + ((YCbCrCbRI * Red + YCbCrCbGI * Green + YCbCrCbBI * Blue + HalfShiftValue) >> Shift));
		*(To + 2) = (uint8_t)(128 + ((YCbCrCrRI * Red + YCbCrCrGI * Green + YCbCrCrBI * Blue + HalfShiftValue) >> Shift));
		From += 4;
		To += 3;
	}
}
