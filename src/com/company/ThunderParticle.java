package com.company;

class ThunderParticle extends Particle {

    private float id;
    private float sigma;
    private float offset;
    private float bkgstd;
    private float chi2;
    private float uncertainity_xy;


    ThunderParticle(float x, float y, float time, float intensity, float id, float sigma, float offset, float bkgstd, float chi2, float uncertainity_xy) {
        super(x, y, time, intensity);
        this.id = id;
        this.sigma = sigma;
        this.offset = offset;
        this.bkgstd = bkgstd;
        this.chi2 = chi2;
        this.uncertainity_xy = uncertainity_xy;

    }

    @Override
    void rescale() {
        super.rescale();
    }

    @Override
    void driftCorrection() {
        super.driftCorrection();
    }

    @Override
    float getX() {
        return super.getX();
    }

    @Override
    float getY() {
        return super.getY();
    }

    @Override
    float getTime() {
        return super.getTime();
    }

    @Override
    float getIntensity() {
        return super.getIntensity();
    }

    @Override
    float getRescaledX() {
        return super.getRescaledX();
    }

    @Override
    float getRescaledY() {
        return super.getRescaledY();
    }

    @Override
    float getNewX() {
        return super.getNewX();
    }

    @Override
    float getNewY() {
        return super.getNewY();
    }


    float getId() {
        return id;
    }

    float getSigma() {
        return sigma;
    }

    float getOffset() {
        return offset;
    }

    float getBkgstd() {
        return bkgstd;
    }

    float getChi2() {
        return chi2;
    }

    float getUncertainity_xy() {
        return uncertainity_xy;
    }
}
